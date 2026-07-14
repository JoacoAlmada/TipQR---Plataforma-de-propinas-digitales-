package tipqr.back.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Valida la firma {@code x-signature} de los webhooks de Mercado Pago.
 * Manifest: {@code id:<data.id>;request-id:<x-request-id>;ts:<ts>;} firmado con HMAC-SHA256.
 * Si no hay secreto configurado, la validación se considera deshabilitada (devuelve true).
 */
@Component
@Slf4j
public class MercadoPagoSignatureValidator {

    private final String secret;

    public MercadoPagoSignatureValidator(@Value("${mercadopago.webhook-secret:}") String secret) {
        this.secret = secret;
    }

    public boolean esValida(String signatureHeader, String requestId, String dataId) {
        if (secret == null || secret.isBlank()) {
            return true; // validación deshabilitada (sin secreto configurado)
        }
        if (signatureHeader == null || signatureHeader.isBlank() || dataId == null) {
            return false;
        }
        Map<String, String> partes = parsearHeader(signatureHeader);
        String ts = partes.get("ts");
        String v1 = partes.get("v1");
        if (ts == null || v1 == null) {
            return false;
        }
        String manifest = "id:" + dataId.toLowerCase() + ";request-id:" + (requestId != null ? requestId : "") + ";ts:" + ts + ";";
        String calculado = hmacSha256Hex(manifest);
        boolean ok = calculado.equalsIgnoreCase(v1);
        if (!ok) {
            log.warn("Firma de webhook MP inválida para data.id={}", dataId);
        }
        return ok;
    }

    private Map<String, String> parsearHeader(String header) {
        Map<String, String> map = new HashMap<>();
        for (String parte : header.split(",")) {
            String[] kv = parte.trim().split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    private String hmacSha256Hex(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular la firma HMAC", e);
        }
    }
}
