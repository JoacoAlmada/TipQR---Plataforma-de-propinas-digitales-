package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.HistorialPropinasResponse;
import tipqr.back.dto.PropinaEmpleadoResponse;
import tipqr.back.dto.RankingEmpleadoResponse;
import tipqr.back.dto.ReportePeriodoResponse;
import tipqr.back.dto.ResumenDuenoResponse;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.DistribucionPropina;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.DistribucionPropinaRepository;
import tipqr.back.repository.OrdenPropinaRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Comparator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lecturas agregadas para los paneles: historial de propinas del empleado y resumen del dueño.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrdenPropinaRepository ordenRepository;
    private final DistribucionPropinaRepository distribucionRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Historial de propinas del empleado: las individuales que recibió directo + la parte que le
     * tocó en las propinas grupales (distribución), unificadas y ordenadas por fecha de pago.
     */
    @Transactional(readOnly = true)
    public HistorialPropinasResponse historialEmpleado(String emailUsuario) {
        Usuario usuario = usuario(emailUsuario);
        Empleado empleado = usuario.getEmpleado();
        if (empleado == null) {
            throw new ResourceNotFoundException("El usuario no tiene un empleado asociado");
        }

        List<PropinaEmpleadoResponse> items = new ArrayList<>();

        // Individuales (la orden apunta directo al empleado).
        ordenRepository.findByEmpleadoIdAndEstadoOrderByFechaPagoDesc(empleado.getId(), EstadoOrden.PAGADA)
                .forEach(o -> items.add(PropinaEmpleadoResponse.fromEntity(o)));

        // Grupales: la parte asignada por el reparto.
        distribucionRepository.findByEmpleadoIdOrderByOrdenPropina_FechaPagoDesc(empleado.getId())
                .forEach(d -> items.add(PropinaEmpleadoResponse.fromDistribucion(d)));

        items.sort(Comparator.comparing(PropinaEmpleadoResponse::getFechaPago,
                Comparator.nullsLast(Comparator.reverseOrder())));

        BigDecimal total = items.stream()
                .map(PropinaEmpleadoResponse::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return HistorialPropinasResponse.builder()
                .cantidad(items.size())
                .total(total)
                .propinas(items)
                .build();
    }

    /** Resumen de propinas pagadas de la empresa del dueño, con desglose por sucursal. */
    @Transactional(readOnly = true)
    public ResumenDuenoResponse resumenDueno(String emailUsuario) {
        Empresa empresa = empresaDe(usuario(emailUsuario));

        List<OrdenPropina> pagadas = ordenRepository
                .findBySucursal_Empresa_IdAndEstado(empresa.getId(), EstadoOrden.PAGADA);

        BigDecimal total = pagadas.stream()
                .map(OrdenPropina::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int cantidad = pagadas.size();
        BigDecimal promedio = cantidad == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);

        // Agregado por sucursal (preservando orden de aparición).
        Map<Long, ResumenDuenoResponse.ResumenSucursal> porSuc = new LinkedHashMap<>();
        for (OrdenPropina o : pagadas) {
            Sucursal s = o.getSucursal();
            if (s == null) continue;
            ResumenDuenoResponse.ResumenSucursal acc = porSuc.get(s.getId());
            if (acc == null) {
                porSuc.put(s.getId(), ResumenDuenoResponse.ResumenSucursal.builder()
                        .sucursalId(s.getId()).sucursalNombre(s.getNombre())
                        .total(o.getMonto()).cantidad(1).build());
            } else {
                porSuc.put(s.getId(), ResumenDuenoResponse.ResumenSucursal.builder()
                        .sucursalId(acc.getSucursalId()).sucursalNombre(acc.getSucursalNombre())
                        .total(acc.getTotal().add(o.getMonto())).cantidad(acc.getCantidad() + 1).build());
            }
        }

        return ResumenDuenoResponse.builder()
                .totalRecaudado(total)
                .cantidadPropinas(cantidad)
                .ticketPromedio(promedio)
                .porSucursal(porSuc.values().stream().toList())
                .build();
    }

    /**
     * Ranking de empleados de la empresa por propinas recibidas: suma sus individuales pagadas
     * más la parte que les tocó en las grupales. Ordenado de mayor a menor.
     */
    @Transactional(readOnly = true)
    public List<RankingEmpleadoResponse> ranking(String emailUsuario) {
        Empresa empresa = empresaDe(usuario(emailUsuario));
        Map<Long, Acc> porEmpleado = new LinkedHashMap<>();

        // Individuales: la orden apunta al empleado.
        ordenRepository.findBySucursal_Empresa_IdAndEstado(empresa.getId(), EstadoOrden.PAGADA).stream()
                .filter(o -> o.getEmpleado() != null)
                .forEach(o -> acumular(porEmpleado, o.getEmpleado(), o.getMonto()));

        // Grupales: lo repartido a cada empleado.
        distribucionRepository.findByOrdenPropina_Sucursal_Empresa_Id(empresa.getId())
                .forEach(d -> acumular(porEmpleado, d.getEmpleado(), d.getMontoAsignado()));

        return porEmpleado.values().stream()
                .sorted(Comparator.comparing(Acc::total).reversed())
                .map(a -> RankingEmpleadoResponse.builder()
                        .empleadoId(a.empleadoId).nombre(a.nombre).sucursal(a.sucursal)
                        .cantidad(a.cantidad).total(a.total).build())
                .toList();
    }

    /** Reporte de propinas pagadas en un rango de fechas (inclusive). */
    @Transactional(readOnly = true)
    public ReportePeriodoResponse reportePeriodo(String emailUsuario, LocalDate desde, LocalDate hasta) {
        Empresa empresa = empresaDe(usuario(emailUsuario));
        LocalDateTime ini = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay(); // fin exclusivo (todo el día "hasta")

        List<OrdenPropina> pagadas = ordenRepository
                .findBySucursal_Empresa_IdAndEstadoAndFechaPagoBetween(
                        empresa.getId(), EstadoOrden.PAGADA, ini, fin);

        BigDecimal total = pagadas.stream().map(OrdenPropina::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        int cantidad = pagadas.size();
        BigDecimal promedio = cantidad == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);

        return ReportePeriodoResponse.builder()
                .desde(desde).hasta(hasta)
                .totalRecaudado(total).cantidadPropinas(cantidad).ticketPromedio(promedio)
                .build();
    }

    // ── Helpers ─────────────────────────────────────────

    private void acumular(Map<Long, Acc> mapa, tipqr.back.entity.Empleado emp, BigDecimal monto) {
        Acc a = mapa.computeIfAbsent(emp.getId(), k -> new Acc(emp.getId(),
                emp.getNombreVisible(),
                emp.getSucursal() != null ? emp.getSucursal().getNombre() : null));
        a.total = a.total.add(monto);
        a.cantidad++;
    }

    /** Acumulador mutable para el ranking. */
    private static final class Acc {
        final Long empleadoId;
        final String nombre;
        final String sucursal;
        BigDecimal total = BigDecimal.ZERO;
        int cantidad = 0;
        Acc(Long id, String nombre, String sucursal) {
            this.empleadoId = id; this.nombre = nombre; this.sucursal = sucursal;
        }
        BigDecimal total() { return total; }
    }

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
