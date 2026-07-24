package tipqr.back.service.ia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.RankingEmpleadoResponse;
import tipqr.back.dto.ReporteAutomaticoResponse;
import tipqr.back.dto.ResumenDuenoResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.ReporteAutomatico;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.ReporteAutomaticoRepository;
import tipqr.back.repository.UsuarioRepository;
import tipqr.back.service.DashboardService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Agente de reportes automáticos: toma las métricas de propinas de la empresa (resumen +
 * ranking) y genera un resumen ejecutivo en lenguaje natural, con panorama, desempeño por
 * sucursal, top de empleados y una recomendación/alerta. El reporte se persiste para que el
 * dueño consulte el historial.
 *
 * Si el proveedor de IA no está configurado o falla, se arma un resumen local de respaldo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgenteReporteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProveedorIa proveedorIa;
    private final DashboardService dashboardService;
    private final ReporteAutomaticoRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;

    private static final String INSTRUCCION_SISTEMA = """
            Sos un analista de negocios que le prepara al dueño de un comercio gastronómico un
            informe ejecutivo sobre el desempeño de las propinas. Escribís en español rioplatense:
            claro, profesional y accionable.

            Reglas:
            - Usá EXCLUSIVAMENTE los números que te paso. No inventes datos ni porcentajes que no
              se puedan derivar de ellos.
            - Formato Markdown: encabezá cada sección con "## ", resaltá los números clave con
              **negrita** y usá listas con "- " o numeradas cuando corresponda.
            - Desarrollá el informe con estas secciones, en este orden:
              1. ## Panorama general — total recaudado, cantidad de propinas y ticket promedio, con
                 una lectura breve de lo que muestran esos números.
              2. ## Desempeño por sucursal — cada sucursal con su total, cantidad y participación
                 (%), e indicá cuál concentra la mayor parte de la recaudación.
              3. ## Empleados destacados — el top del ranking con su aporte y su peso relativo (%).
              4. ## Observaciones — 2 o 3 lecturas o tendencias que se desprendan de los datos
                 (concentración, dispersión, sucursales sin actividad, etc.).
              5. ## Recomendaciones — 2 o 3 acciones concretas y priorizadas para mejorar la
                 recaudación.
            - Desarrollá cada sección lo suficiente para que sea útil, sin relleno ni repeticiones.""";

    /** Genera y persiste un reporte del estado actual de propinas de la empresa. */
    @Transactional
    public ReporteAutomaticoResponse generar(String email) {
        Usuario usuario = usuario(email);
        Empresa empresa = empresaDe(usuario);

        ResumenDuenoResponse resumen = dashboardService.resumenDueno(email);
        List<RankingEmpleadoResponse> ranking = dashboardService.ranking(email);

        String contenido;
        boolean porIa = false;
        if (proveedorIa.disponible()) {
            try {
                contenido = conIa(resumen, ranking);
                porIa = true;
            } catch (IaException e) {
                log.warn("Agente de reportes: fallback local por error de IA: {}", e.getMessage());
                contenido = respaldoLocal(resumen, ranking);
            }
        } else {
            contenido = respaldoLocal(resumen, ranking);
        }

        ReporteAutomatico reporte = reporteRepository.save(ReporteAutomatico.builder()
                .empresa(empresa)
                .titulo("Resumen de propinas — " + LocalDate.now().format(FECHA))
                .resumenGenerado(contenido)
                .totalRecaudado(resumen.getTotalRecaudado())
                .cantidadPropinas(resumen.getCantidadPropinas())
                .ticketPromedio(resumen.getTicketPromedio())
                .generadoPorIa(porIa)
                .build());

        return ReporteAutomaticoResponse.fromEntity(reporte);
    }

    /** Historial de reportes de la empresa, del más nuevo al más viejo. */
    @Transactional(readOnly = true)
    public List<ReporteAutomaticoResponse> listar(String email) {
        Empresa empresa = empresaDe(usuario(email));
        return reporteRepository.findByEmpresaIdOrderByFechaGeneracionDesc(empresa.getId())
                .stream().map(ReporteAutomaticoResponse::fromEntity).toList();
    }

    /** Elimina un reporte de la empresa. */
    @Transactional
    public void eliminar(Long reporteId, String email) {
        Empresa empresa = empresaDe(usuario(email));
        ReporteAutomatico reporte = reporteRepository.findByIdAndEmpresaId(reporteId, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reporte", reporteId));
        reporteRepository.delete(reporte);
    }

    // ── Camino con IA ─────────────────────────────────────────────────────────

    private String conIa(ResumenDuenoResponse resumen, List<RankingEmpleadoResponse> ranking) {
        String prompt = "Datos de propinas de la empresa:\n" + datosTexto(resumen, ranking)
                + "\nRedactá el resumen ejecutivo para el dueño.";
        return proveedorIa.completar(INSTRUCCION_SISTEMA, prompt, false);
    }

    private String datosTexto(ResumenDuenoResponse r, List<RankingEmpleadoResponse> ranking) {
        BigDecimal total = r.getTotalRecaudado() != null ? r.getTotalRecaudado() : BigDecimal.ZERO;
        StringBuilder sb = new StringBuilder();
        sb.append("- Total recaudado: $").append(total).append('\n');
        sb.append("- Cantidad de propinas: ").append(r.getCantidadPropinas()).append('\n');
        sb.append("- Ticket promedio: $").append(r.getTicketPromedio()).append('\n');
        sb.append("- Sucursales con propinas: ").append(r.getPorSucursal().size()).append('\n');
        sb.append("Detalle por sucursal (con participación sobre el total):\n");
        if (r.getPorSucursal().isEmpty()) {
            sb.append("  (sin sucursales con propinas)\n");
        } else {
            r.getPorSucursal().forEach(s -> sb.append("  - ").append(s.getSucursalNombre())
                    .append(": $").append(s.getTotal())
                    .append(" (").append(s.getCantidad()).append(" propinas, ")
                    .append(pct(s.getTotal(), total)).append("% del total)\n"));
        }
        sb.append("Ranking de empleados (top 5, con aporte y participación):\n");
        if (ranking.isEmpty()) {
            sb.append("  (sin datos)\n");
        } else {
            ranking.stream().limit(5).forEach(e -> sb.append("  - ").append(e.getNombre())
                    .append(" (").append(e.getSucursal() != null ? e.getSucursal() : "s/sucursal").append("): $")
                    .append(e.getTotal()).append(" en ").append(e.getCantidad()).append(" propinas, ")
                    .append(pct(e.getTotal(), total)).append("% del total\n"));
        }
        return sb.toString();
    }

    /** Participación porcentual de una parte sobre el total (1 decimal), robusto ante total 0. */
    private String pct(BigDecimal parte, BigDecimal total) {
        if (parte == null || total == null || total.signum() == 0) return "0";
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP).toPlainString();
    }

    // ── Resumen local de respaldo ─────────────────────────────────────────────

    private String respaldoLocal(ResumenDuenoResponse r, List<RankingEmpleadoResponse> ranking) {
        BigDecimal total = r.getTotalRecaudado() != null ? r.getTotalRecaudado() : BigDecimal.ZERO;
        StringBuilder sb = new StringBuilder();
        sb.append("## Panorama general\n");
        if (r.getCantidadPropinas() == 0) {
            sb.append("Todavía no hay propinas pagadas registradas. Cuando empiecen a entrar, ")
                    .append("vas a ver acá el total, el promedio y el desempeño por sucursal.\n");
            return sb.toString();
        }
        sb.append("Se recaudaron **$").append(total).append("** en **")
                .append(r.getCantidadPropinas()).append("** propinas, con un ticket promedio de **$")
                .append(r.getTicketPromedio()).append("**.\n\n");

        sb.append("## Desempeño por sucursal\n");
        if (r.getPorSucursal().isEmpty()) {
            sb.append("- Sin desglose por sucursal.\n");
        } else {
            ResumenDuenoResponse.ResumenSucursal top = r.getPorSucursal().stream()
                    .max((a, b) -> a.getTotal().compareTo(b.getTotal())).orElse(null);
            r.getPorSucursal().forEach(s -> sb.append("- **").append(s.getSucursalNombre())
                    .append("**: $").append(s.getTotal()).append(" (").append(s.getCantidad())
                    .append(" propinas, ").append(pct(s.getTotal(), total)).append("% del total)\n"));
            if (top != null) {
                sb.append("\nLa sucursal que más recaudó fue **").append(top.getSucursalNombre())
                        .append("** (").append(pct(top.getTotal(), total)).append("% del total).\n");
            }
        }
        sb.append('\n');

        sb.append("## Empleados destacados\n");
        if (ranking.isEmpty()) {
            sb.append("- Sin datos de empleados.\n");
        } else {
            ranking.stream().limit(5).forEach(e -> sb.append("- **").append(e.getNombre())
                    .append("**: $").append(e.getTotal()).append(" en ").append(e.getCantidad())
                    .append(" propinas (").append(pct(e.getTotal(), total)).append("% del total)\n"));
            sb.append("\nReconocé a **").append(ranking.get(0).getNombre())
                    .append("**, que lidera el ranking del período.\n");
        }
        sb.append('\n');

        sb.append("## Observaciones\n");
        int sucs = r.getPorSucursal().size();
        if (sucs <= 1) {
            sb.append("- La recaudación está concentrada en una sola sucursal: conviene revisar si el ")
                    .append("resto de los locales tiene el QR visible y operativo.\n");
        } else {
            sb.append("- Hay actividad de propinas en ").append(sucs).append(" sucursales.\n");
        }
        if (!ranking.isEmpty() && total.signum() > 0) {
            sb.append("- El principal aportante concentra el ").append(pct(ranking.get(0).getTotal(), total))
                    .append("% de lo recaudado.\n");
        }
        sb.append('\n');

        sb.append("## Recomendaciones\n");
        BigDecimal promedio = r.getTicketPromedio();
        if (promedio != null && promedio.compareTo(BigDecimal.valueOf(300)) < 0) {
            sb.append("- El ticket promedio es bajo: reforzá la sugerencia de propina en la mesa y al momento de pagar.\n");
        } else {
            sb.append("- Buen nivel de propinas: mantené la calidad de atención y compartí estos números con el equipo.\n");
        }
        sb.append("- Compartí el ranking con el equipo para reconocer a quienes más aportan y motivar al resto.\n");
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Empresa empresaDe(Usuario usuario) {
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }
}
