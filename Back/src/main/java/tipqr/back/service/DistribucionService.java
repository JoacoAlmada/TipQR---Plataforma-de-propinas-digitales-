package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.entity.DistribucionPropina;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.GrupoPropinaEmpleado;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.entity.enums.TipoDistribucion;
import tipqr.back.entity.enums.TipoEventoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.repository.DistribucionPropinaRepository;
import tipqr.back.repository.EventoOrdenRepository;
import tipqr.back.repository.GrupoPropinaEmpleadoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reparte una propina GRUPAL pagada entre los miembros del grupo.
 * El grupo define el criterio: EQUITATIVO (partes iguales) o PORCENTAJE (según el % de cada miembro).
 * En ambos casos el reparto es exacto al centavo: los centavos que no dividen justo se asignan por
 * método de mayor resto (a igualdad, al primero), así la suma cierra igual al total.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistribucionService {

    private final GrupoPropinaEmpleadoRepository miembroRepository;
    private final DistribucionPropinaRepository distribucionRepository;
    private final EventoOrdenRepository eventoRepository;
    private final NotificacionService notificacionService;

    /**
     * Genera el reparto de una orden. Solo actúa si la orden es GRUPAL, tiene grupo y aún no fue repartida.
     */
    @Transactional
    public void distribuir(OrdenPropina orden) {
        if (orden.getTipoPropina() != TipoPropina.GRUPAL) {
            return; // las individuales van directo al empleado, no se reparten
        }
        GrupoPropina grupo = orden.getGrupoPropina();
        if (grupo == null) {
            log.warn("Orden grupal {} sin grupo asignado; no se puede repartir", orden.getCodigo());
            return;
        }
        if (distribucionRepository.existsByOrdenPropinaId(orden.getId())) {
            return; // idempotente: ya repartida
        }

        List<GrupoPropinaEmpleado> miembros = miembroRepository
                .findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(grupo.getId())
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .toList();

        if (miembros.isEmpty()) {
            log.warn("Grupo {} sin miembros activos; la propina {} queda sin repartir",
                    grupo.getId(), orden.getCodigo());
            return;
        }

        boolean porPorcentaje = usaPorcentajes(grupo, miembros);
        List<BigDecimal> pesos = porPorcentaje
                ? miembros.stream().map(m -> BigDecimal.valueOf(m.getPorcentajeDistribucion())).toList()
                : miembros.stream().map(m -> BigDecimal.ONE).toList(); // equitativo = pesos iguales

        long totalCent = orden.getMonto().movePointRight(2).longValueExact();
        long[] centavos = repartirCentavos(totalCent, pesos);
        String criterio = porPorcentaje ? "PORCENTAJE" : "EQUITATIVO";
        BigDecimal totalPesos = pesos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        for (int i = 0; i < miembros.size(); i++) {
            BigDecimal parte = BigDecimal.valueOf(centavos[i]).movePointLeft(2);
            double pct = pesos.get(i).multiply(BigDecimal.valueOf(100))
                    .divide(totalPesos, 2, RoundingMode.HALF_UP).doubleValue();
            distribucionRepository.save(DistribucionPropina.builder()
                    .ordenPropina(orden)
                    .empleado(miembros.get(i).getEmpleado())
                    .montoAsignado(parte)
                    .porcentaje(pct)
                    .criterio(criterio)
                    .build());
            notificacionService.notificarPropinaRecibida(miembros.get(i).getEmpleado(), parte,
                    "(parte de una propina grupal de " + grupo.getNombre() + ")");
        }

        eventoRepository.save(tipqr.back.entity.EventoOrden.builder()
                .ordenPropina(orden)
                .tipoEvento(TipoEventoOrden.DISTRIBUCION_GENERADA)
                .descripcion("Repartida (" + criterio.toLowerCase() + ") entre "
                        + miembros.size() + " integrantes de " + grupo.getNombre())
                .build());

        log.info("Propina {} repartida ({}) entre {} integrantes de {}",
                orden.getCodigo(), criterio, miembros.size(), grupo.getNombre());
    }

    /** True si el grupo está en modo PORCENTAJE y todos los miembros tienen % válido que suma ~100. */
    private boolean usaPorcentajes(GrupoPropina grupo, List<GrupoPropinaEmpleado> miembros) {
        if (grupo.getTipoDistribucion() != TipoDistribucion.PORCENTAJE) {
            return false;
        }
        BigDecimal suma = BigDecimal.ZERO;
        for (GrupoPropinaEmpleado m : miembros) {
            Double p = m.getPorcentajeDistribucion();
            if (p == null || p <= 0) {
                log.warn("Grupo {} en modo PORCENTAJE pero un miembro no tiene %; se usa equitativo", grupo.getId());
                return false;
            }
            suma = suma.add(BigDecimal.valueOf(p));
        }
        // tolerancia por si hay decimales
        if (suma.subtract(BigDecimal.valueOf(100)).abs().compareTo(new BigDecimal("0.5")) > 0) {
            log.warn("Grupo {}: los porcentajes suman {} (≠100); se usa equitativo", grupo.getId(), suma);
            return false;
        }
        return true;
    }

    /**
     * Reparte {@code totalCent} centavos según {@code pesos}, devolviendo enteros que suman exacto.
     * Usa el método de mayor resto: cada uno recibe la parte entera de su cuota y los centavos
     * sobrantes van a los de mayor resto (a igualdad, al de menor índice).
     */
    private long[] repartirCentavos(long totalCent, List<BigDecimal> pesos) {
        BigDecimal totalPesos = pesos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        int n = pesos.size();
        long[] base = new long[n];
        long asignado = 0;
        List<BigDecimal> fracs = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            BigDecimal cuota = BigDecimal.valueOf(totalCent).multiply(pesos.get(i))
                    .divide(totalPesos, 6, RoundingMode.HALF_UP);
            long entero = cuota.setScale(0, RoundingMode.FLOOR).longValueExact();
            base[i] = entero;
            asignado += entero;
            fracs.add(cuota.subtract(BigDecimal.valueOf(entero)));
            indices.add(i);
        }

        long sobrante = totalCent - asignado;
        // Ordeno los índices por resto descendente; a igualdad, por índice ascendente.
        indices.sort(Comparator
                .<Integer>comparingDouble(i -> -fracs.get(i).doubleValue())
                .thenComparingInt(i -> i));
        for (int k = 0; k < sobrante && k < n; k++) {
            base[indices.get(k)]++;
        }
        return base;
    }
}
