package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import tipqr.back.dto.CrearOrdenPublicaRequest;
import tipqr.back.dto.MesaDestinatariosResponse;
import tipqr.back.dto.OrdenEstadoResponse;
import tipqr.back.dto.PagoIniciadoResponse;
import tipqr.back.dto.QrDestinoResponse;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.service.OrdenService;
import tipqr.back.service.PagoService;
import tipqr.back.service.QrService;

import java.util.Map;

/**
 * Endpoints públicos de la pantalla del cliente (sin login): escanear QR y dejar propina.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final QrService qrService;
    private final OrdenService ordenService;
    private final PagoService pagoService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    /** Resuelve el QR escaneado para mostrar a quién se le deja la propina. */
    @GetMapping("/qr/{codigo}")
    public ResponseEntity<QrDestinoResponse> resolverQr(@PathVariable String codigo) {
        return ResponseEntity.ok(qrService.resolverPublico(codigo));
    }

    /** Para un QR de mesa: lista los mozos del turno activo (para elegir) o indica que no hay turno. */
    @GetMapping("/qr/{codigo}/destinatarios")
    public ResponseEntity<MesaDestinatariosResponse> destinatariosMesa(@PathVariable String codigo) {
        return ResponseEntity.ok(qrService.resolverDestinatariosMesa(codigo));
    }

    /** Crea la orden de propina con el monto elegido por el cliente. */
    @PostMapping("/qr/{codigo}/ordenes")
    public ResponseEntity<OrdenEstadoResponse> crearOrden(
            @PathVariable String codigo,
            @Valid @RequestBody CrearOrdenPublicaRequest request) {
        OrdenPropina orden = ordenService.crearDesdeQr(codigo, request.getMonto(), request.getEmpleadoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdenEstadoResponse.fromEntity(orden));
    }

    /** Inicia el pago de una orden: crea la preferencia y devuelve la URL del Checkout Pro. */
    @PostMapping("/ordenes/{codigo}/pago")
    public ResponseEntity<PagoIniciadoResponse> iniciarPago(@PathVariable String codigo) {
        return ResponseEntity.ok(pagoService.iniciarPago(codigo));
    }

    /**
     * Retorno del Checkout Pro: MP redirige acá (URL pública) con el resultado del pago.
     * Conciliamos con el payment_id (sin depender del webhook, poco confiable en sandbox)
     * y reenviamos al front.
     */
    @GetMapping("/pagos/retorno")
    public ResponseEntity<Void> retornoPago(
            @RequestParam String orden,
            @RequestParam(name = "payment_id", required = false) String paymentId) {
        pagoService.conciliarDesdeRetorno(paymentId);
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        URI destino = URI.create(base + "/pago/resultado?orden=" + orden);
        return ResponseEntity.status(HttpStatus.FOUND).location(destino).build();
    }

    /** Webhook de Mercado Pago: notifica el resultado del pago para conciliar la orden. */
    @PostMapping("/pagos/webhook")
    public ResponseEntity<Void> webhookPago(
            @RequestParam(required = false) Map<String, String> params,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestHeader(value = "x-request-id", required = false) String requestId,
            @RequestBody(required = false) String rawBody) {
        String tipo = params.getOrDefault("type", params.get("topic"));
        String dataId = params.getOrDefault("data.id", params.get("id"));
        pagoService.procesarWebhook(tipo, dataId, requestId, signature, rawBody);
        return ResponseEntity.ok().build();
    }
}
