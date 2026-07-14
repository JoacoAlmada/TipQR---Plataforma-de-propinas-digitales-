package tipqr.back.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Cliente REST de Mercado Pago (Checkout Pro). Crea preferencias de pago y consulta pagos.
 * Se usa el access token del comercio (vendedor de prueba en sandbox).
 */
@Component
@Slf4j
public class MercadoPagoClient {

    private final RestClient rest;
    private final String accessToken;

    public MercadoPagoClient(
            @Value("${mercadopago.access-token:}") String accessToken,
            @Value("${mercadopago.api-base:https://api.mercadopago.com}") String apiBase) {
        this.accessToken = accessToken;
        this.rest = RestClient.builder().baseUrl(apiBase).build();
    }

    /** Crea una preferencia de Checkout Pro y devuelve su id + el init_point (URL del checkout). */
    public PreferenciaCreada crearPreferencia(String titulo, BigDecimal monto, String externalReference,
                                              String notificationUrl, String backUrl) {
        Map<String, Object> item = Map.of(
                "title", titulo,
                "quantity", 1,
                "unit_price", monto,
                "currency_id", "ARS");
        Map<String, Object> backUrls = Map.of(
                "success", backUrl,
                "failure", backUrl,
                "pending", backUrl);
        Map<String, Object> body = Map.of(
                "items", List.of(item),
                "external_reference", externalReference,
                "notification_url", notificationUrl,
                "back_urls", backUrls,
                "auto_return", "approved");

        JsonNode resp = rest.post().uri("/checkout/preferences")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (resp == null || resp.get("id") == null) {
            throw new IllegalStateException("Mercado Pago no devolvió la preferencia");
        }
        return new PreferenciaCreada(resp.get("id").asText(), resp.path("init_point").asText());
    }

    /** Consulta el estado real de un pago en Mercado Pago. */
    public PagoMp obtenerPago(String paymentId) {
        JsonNode resp = rest.get().uri("/v1/payments/{id}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(JsonNode.class);

        if (resp == null || resp.get("id") == null) {
            throw new IllegalStateException("Mercado Pago no devolvió el pago " + paymentId);
        }
        return new PagoMp(
                resp.get("id").asText(),
                resp.path("status").asText(null),
                resp.path("external_reference").asText(null),
                resp.path("transaction_amount").isMissingNode() ? null : resp.path("transaction_amount").decimalValue());
    }

    public record PreferenciaCreada(String preferenceId, String initPoint) {}

    public record PagoMp(String id, String status, String externalReference, BigDecimal monto) {}
}
