package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.PagoIniciadoResponse;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.entity.Pago;
import tipqr.back.entity.WebhookPago;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.OrdenPropinaRepository;
import tipqr.back.repository.PagoRepository;
import tipqr.back.repository.WebhookPagoRepository;

import java.util.Set;

/**
 * Orquesta el pago de una orden con Mercado Pago: crea la preferencia de Checkout Pro
 * y concilia la orden cuando llega el webhook con el resultado del pago.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private static final Set<EstadoOrden> FINALES = Set.of(
            EstadoOrden.PAGADA, EstadoOrden.RECHAZADA, EstadoOrden.CANCELADA, EstadoOrden.EXPIRADA);

    private final OrdenPropinaRepository ordenRepository;
    private final PagoRepository pagoRepository;
    private final WebhookPagoRepository webhookRepository;
    private final OrdenService ordenService;
    private final DistribucionService distribucionService;
    private final NotificacionService notificacionService;
    private final MercadoPagoClient mpClient;
    private final MercadoPagoSignatureValidator signatureValidator;

    @Value("${mercadopago.public-key:}")
    private String publicKey;

    /** En sandbox los webhooks de los usuarios de prueba se firman con un secreto no expuesto,
     *  por eso la validación es configurable. En producción debe estar en true. */
    @Value("${mercadopago.webhook-validate-signature:true}")
    private boolean validarFirma;

    @Value("${mercadopago.webhook-url:}")
    private String webhookUrl;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    // ── Iniciar pago ──────────────────────────────────────────────────────────

    @Transactional
    public PagoIniciadoResponse iniciarPago(String ordenCodigo) {
        OrdenPropina orden = ordenRepository.findByCodigo(ordenCodigo)
                .orElseThrow(() -> new ResourceNotFoundException("Orden " + ordenCodigo + " no encontrada"));

        if (FINALES.contains(orden.getEstado())) {
            throw new IllegalArgumentException(
                    "La orden está en estado " + orden.getEstado() + " y no admite pago");
        }

        String notificationUrl = trimSlash(webhookUrl) + "/api/public/pagos/webhook";
        // MP exige back_urls públicas (https) con auto_return; usamos el túnel y redirigimos al front.
        String backUrl = trimSlash(webhookUrl) + "/api/public/pagos/retorno?orden=" + ordenCodigo;
        String titulo = "Propina TipQR - " + destinoLabel(orden);

        MercadoPagoClient.PreferenciaCreada pref = mpClient.crearPreferencia(
                titulo, orden.getMonto(), ordenCodigo, notificationUrl, backUrl);

        Pago pago = pagoRepository.findByOrdenPropinaId(orden.getId()).orElseGet(Pago::new);
        pago.setOrdenPropina(orden);
        pago.setProveedor("MERCADO_PAGO");
        pago.setPreferenceId(pref.preferenceId());
        pago.setInitPoint(pref.initPoint());
        pago.setMonto(orden.getMonto());
        pago.setEstadoProveedor("pending");
        pagoRepository.save(pago);

        // La orden queda a la espera del pago.
        if (orden.getEstado() == EstadoOrden.CREADA) {
            ordenService.marcarPendientePago(orden);
        }

        return PagoIniciadoResponse.builder()
                .ordenCodigo(ordenCodigo)
                .preferenceId(pref.preferenceId())
                .checkoutUrl(pref.initPoint())
                .publicKey(publicKey)
                .build();
    }

    // ── Webhook ───────────────────────────────────────────────────────────────

    @Transactional
    public void procesarWebhook(String tipo, String dataId, String requestId,
                                String signatureHeader, String rawBody) {
        if (!signatureValidator.esValida(signatureHeader, requestId, dataId)) {
            if (validarFirma) {
                throw new IllegalArgumentException("Firma de webhook inválida");
            }
            log.warn("Firma de webhook inválida (validación deshabilitada en este entorno, se procesa igual)");
        }
        // Solo nos interesan las notificaciones de pago.
        if (tipo == null || !tipo.equalsIgnoreCase("payment") || dataId == null) {
            log.info("Webhook MP ignorado (tipo={}, dataId={})", tipo, dataId);
            return;
        }
        conciliarPago(dataId, "webhook", rawBody);
    }

    /**
     * Concilia desde el retorno del Checkout (back_url). En sandbox MP no siempre dispara el
     * webhook, pero el retorno trae el payment_id: lo usamos para confirmar el pago sin depender
     * del webhook. Best-effort: cualquier fallo se loguea y no interrumpe la vuelta del cliente.
     */
    @Transactional
    public void conciliarDesdeRetorno(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return;
        }
        try {
            conciliarPago(paymentId, "retorno", null);
        } catch (Exception e) {
            log.warn("No se pudo conciliar desde el retorno (payment {}): {}", paymentId, e.getMessage());
        }
    }

    /** Núcleo de conciliación: consulta el pago a MP y lleva la orden al estado correspondiente. */
    private void conciliarPago(String paymentId, String origen, String rawBody) {
        MercadoPagoClient.PagoMp mp = mpClient.obtenerPago(paymentId);
        String ordenCodigo = mp.externalReference();
        if (ordenCodigo == null) {
            log.warn("Pago MP {} sin external_reference; se ignora", paymentId);
            return;
        }

        OrdenPropina orden = ordenRepository.findByCodigo(ordenCodigo).orElse(null);
        if (orden == null) {
            log.warn("Notificación MP ({}) para orden inexistente {}", origen, ordenCodigo);
            return;
        }

        Pago pago = pagoRepository.findByOrdenPropinaId(orden.getId()).orElseGet(() -> {
            Pago p = new Pago();
            p.setOrdenPropina(orden);
            p.setProveedor("MERCADO_PAGO");
            p.setMonto(orden.getMonto());
            return p;
        });
        pago.setExternalPaymentId(mp.id());
        pago.setEstadoProveedor(mp.status());
        pago = pagoRepository.save(pago);

        webhookRepository.save(WebhookPago.builder()
                .pago(pago)
                .proveedor("MERCADO_PAGO")
                .tipoEvento(origen)
                .externalId(paymentId)
                .payload(rawBody)
                .procesado(true)
                .build());

        conciliar(orden, mp.status());
    }

    /** Lleva la orden al estado que corresponde según el resultado del pago (idempotente). */
    private void conciliar(OrdenPropina orden, String status) {
        EstadoOrden estado = orden.getEstado();
        if (FINALES.contains(estado)) {
            log.info("Orden {} ya en estado final {}, webhook sin cambios", orden.getCodigo(), estado);
            return;
        }
        if (status == null) {
            return;
        }
        switch (status) {
            case "approved" -> {
                ordenService.marcarPagada(orden);
                // Individual: avisar al empleado. Grupal: se reparte y se avisa a cada uno en el reparto.
                if (orden.getEmpleado() != null) {
                    String detalle = orden.getMesa() != null ? "(Mesa " + orden.getMesa().getNumero() + ")" : null;
                    notificacionService.notificarPropinaRecibida(orden.getEmpleado(), orden.getMonto(), detalle);
                }
                distribucionService.distribuir(orden);
            }
            case "rejected", "cancelled" -> ordenService.marcarRechazada(orden, "Pago " + status);
            case "pending", "in_process", "authorized" -> {
                if (estado == EstadoOrden.CREADA) {
                    ordenService.marcarPendientePago(orden);
                }
            }
            default -> log.info("Estado de pago MP no contemplado: {}", status);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String destinoLabel(OrdenPropina orden) {
        if (orden.getEmpleado() != null) return orden.getEmpleado().getNombreVisible();
        if (orden.getMesa() != null) return "Mesa " + orden.getMesa().getNumero();
        if (orden.getGrupoPropina() != null) return orden.getGrupoPropina().getNombre();
        return orden.getSucursal() != null ? orden.getSucursal().getNombre() : "Propina";
    }

    private String trimSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
