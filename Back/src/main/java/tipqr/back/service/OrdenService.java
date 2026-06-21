package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.OrdenEstadoResponse;
import tipqr.back.entity.*;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.TipoDestinoQR;
import tipqr.back.entity.enums.TipoEventoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.CodigoQRRepository;
import tipqr.back.repository.EventoOrdenRepository;
import tipqr.back.repository.OrdenPropinaRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Dominio de las órdenes de propina: creación, ciclo de estados y registro de eventos.
 *
 * Ciclo: CREADA → PENDIENTE_PAGO → PAGADA / RECHAZADA / CANCELADA / EXPIRADA.
 * Los estados PAGADA, RECHAZADA, CANCELADA y EXPIRADA son finales (no admiten más transiciones).
 * Cada cambio de estado deja un EventoOrden para tener trazabilidad completa.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdenService {

    private static final Set<EstadoOrden> ESTADOS_FINALES = Set.of(
            EstadoOrden.PAGADA, EstadoOrden.RECHAZADA,
            EstadoOrden.CANCELADA, EstadoOrden.EXPIRADA);

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LARGO_CODIGO = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrdenPropinaRepository ordenRepository;
    private final EventoOrdenRepository eventoRepository;
    private final CodigoQRRepository qrRepository;

    @Value("${tipqr.orden.vencimiento-minutos:15}")
    private long vencimientoMinutos;

    // ── Creación ────────────────────────────────────────────────────────────

    /**
     * Crea una orden en estado CREADA, con código único y vencimiento, y registra el evento.
     * La asociación (mesa / empleado / grupo) llega ya resuelta por quien la crea (QR, pantalla pública).
     */
    @Transactional
    public OrdenPropina crearOrden(Sucursal sucursal, TipoPropina tipoPropina, BigDecimal monto,
                                   Mesa mesa, Empleado empleado, GrupoPropina grupo) {
        if (sucursal == null) {
            throw new IllegalArgumentException("La orden requiere una sucursal");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la propina debe ser mayor a cero");
        }
        if (tipoPropina == TipoPropina.INDIVIDUAL && empleado == null && mesa == null) {
            throw new IllegalArgumentException("Una propina individual debe asociarse a un empleado o una mesa");
        }
        if (tipoPropina == TipoPropina.GRUPAL && grupo == null) {
            throw new IllegalArgumentException("Una propina grupal debe asociarse a un grupo");
        }

        LocalDateTime ahora = LocalDateTime.now();
        OrdenPropina orden = OrdenPropina.builder()
                .codigo(generarCodigoUnico())
                .sucursal(sucursal)
                .mesa(mesa)
                .empleado(empleado)
                .grupoPropina(grupo)
                .tipoPropina(tipoPropina)
                .monto(monto)
                .estado(EstadoOrden.CREADA)
                .fechaExpiracion(ahora.plusMinutes(vencimientoMinutos))
                .build();

        orden = ordenRepository.save(orden);
        registrarEvento(orden, TipoEventoOrden.ORDEN_CREADA, "Orden creada por " + monto);
        return orden;
    }

    /**
     * Crea una orden a partir de un código QR escaneado (pantalla pública).
     * El destino del QR define el tipo de propina y las asociaciones.
     */
    @Transactional
    public OrdenPropina crearDesdeQr(String qrCodigo, BigDecimal monto) {
        CodigoQR qr = qrRepository.findByCodigo(qrCodigo)
                .filter(q -> Boolean.TRUE.equals(q.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Código QR " + qrCodigo + " no encontrado o inactivo"));

        TipoDestinoQR destino = qr.getTipoDestino();
        return switch (destino) {
            case EMPLEADO -> crearOrden(qr.getSucursal(), TipoPropina.INDIVIDUAL, monto,
                    null, qr.getEmpleado(), null);
            case MESA -> crearOrden(qr.getSucursal(), TipoPropina.INDIVIDUAL, monto,
                    qr.getMesa(), null, null);
            case GRUPO -> crearOrden(qr.getSucursal(), TipoPropina.GRUPAL, monto,
                    null, null, qr.getGrupoPropina());
            default -> throw new IllegalArgumentException(
                    "El tipo de destino " + destino + " no admite propinas todavía");
        };
    }

    // ── Transiciones de estado ──────────────────────────────────────────────

    /** Pasa la orden a PENDIENTE_PAGO (al generar la preferencia de pago). */
    @Transactional
    public OrdenPropina marcarPendientePago(OrdenPropina orden) {
        transicionar(orden, EstadoOrden.PENDIENTE_PAGO,
                TipoEventoOrden.PREFERENCIA_MP_GENERADA, "A la espera del pago");
        return ordenRepository.save(orden);
    }

    /** Confirma el pago de la orden. */
    @Transactional
    public OrdenPropina marcarPagada(OrdenPropina orden) {
        registrarEvento(orden, TipoEventoOrden.PAGO_CONFIRMADO, "Pago confirmado");
        orden.setFechaPago(LocalDateTime.now());
        transicionar(orden, EstadoOrden.PAGADA, TipoEventoOrden.ORDEN_PAGADA, "Orden pagada");
        return ordenRepository.save(orden);
    }

    /** Marca la orden como rechazada (pago rechazado). */
    @Transactional
    public OrdenPropina marcarRechazada(OrdenPropina orden, String motivo) {
        transicionar(orden, EstadoOrden.RECHAZADA, TipoEventoOrden.ORDEN_RECHAZADA,
                motivo != null ? motivo : "Pago rechazado");
        return ordenRepository.save(orden);
    }

    /** Cancela la orden (por el comercio o por timeout manual). */
    @Transactional
    public OrdenPropina cancelar(OrdenPropina orden, String motivo) {
        transicionar(orden, EstadoOrden.CANCELADA, TipoEventoOrden.ORDEN_CANCELADA,
                motivo != null ? motivo : "Orden cancelada");
        return ordenRepository.save(orden);
    }

    // ── Expiración automática ───────────────────────────────────────────────

    /**
     * Expira las órdenes sin pagar cuyo vencimiento ya pasó. Corre periódicamente.
     * El intervalo y el vencimiento son configurables (application.properties).
     */
    @Scheduled(fixedDelayString = "${tipqr.orden.expiracion-check-ms:60000}")
    @Transactional
    public void expirarOrdenesVencidas() {
        List<OrdenPropina> vencidas = ordenRepository.findByEstadoInAndFechaExpiracionBefore(
                List.of(EstadoOrden.CREADA, EstadoOrden.PENDIENTE_PAGO), LocalDateTime.now());
        if (vencidas.isEmpty()) {
            return;
        }
        for (OrdenPropina orden : vencidas) {
            transicionar(orden, EstadoOrden.EXPIRADA, TipoEventoOrden.ORDEN_EXPIRADA,
                    "Expiró sin pago dentro del plazo");
            ordenRepository.save(orden);
        }
        log.info("Expiradas {} órdenes vencidas", vencidas.size());
    }

    // ── Consulta pública ────────────────────────────────────────────────────

    /** Estado de una orden por su código — usado por la pantalla pública mientras espera el pago. */
    @Transactional(readOnly = true)
    public OrdenEstadoResponse consultarEstadoPublico(String codigo) {
        OrdenPropina orden = ordenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Orden " + codigo + " no encontrada"));
        return OrdenEstadoResponse.fromEntity(orden);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void transicionar(OrdenPropina orden, EstadoOrden nuevoEstado,
                              TipoEventoOrden tipoEvento, String descripcion) {
        if (ESTADOS_FINALES.contains(orden.getEstado())) {
            throw new IllegalArgumentException(
                    "La orden ya está en estado final " + orden.getEstado() + " y no puede cambiar");
        }
        orden.setEstado(nuevoEstado);
        registrarEvento(orden, tipoEvento, descripcion);
    }

    private void registrarEvento(OrdenPropina orden, TipoEventoOrden tipo, String descripcion) {
        eventoRepository.save(EventoOrden.builder()
                .ordenPropina(orden)
                .tipoEvento(tipo)
                .descripcion(descripcion)
                .build());
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder(LARGO_CODIGO);
            for (int i = 0; i < LARGO_CODIGO; i++) {
                sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
            }
            codigo = sb.toString();
        } while (ordenRepository.existsByCodigo(codigo));
        return codigo;
    }
}
