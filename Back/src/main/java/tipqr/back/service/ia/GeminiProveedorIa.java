package tipqr.back.service.ia;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proveedor de IA implementado sobre la API de Google Gemini (generateContent).
 * Se usa REST directo (mismo enfoque que {@code MercadoPagoClient}); la API key
 * vive en Back/.env (gitignored) y nunca en el repo.
 */
@Component
@Slf4j
public class GeminiProveedorIa implements ProveedorIa {

    private final RestClient rest;
    private final String apiKey;
    private final String model;
    private final boolean enabled;

    public GeminiProveedorIa(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.0-flash}") String model,
            @Value("${gemini.api-base:https://generativelanguage.googleapis.com}") String apiBase,
            @Value("${gemini.enabled:true}") boolean enabled) {
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(20).toMillis());
        this.rest = RestClient.builder().baseUrl(apiBase).requestFactory(factory).build();
    }

    @Override
    public boolean disponible() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String completar(String instruccionSistema, String prompt, boolean jsonMode) {
        if (!disponible()) {
            throw new IaException("El proveedor de IA no está configurado (falta GEMINI_API_KEY).");
        }

        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("temperature", 0.4);
        genConfig.put("maxOutputTokens", 700);
        if (jsonMode) {
            genConfig.put("responseMimeType", "application/json");
        }

        Map<String, Object> body = new HashMap<>();
        if (instruccionSistema != null && !instruccionSistema.isBlank()) {
            body.put("system_instruction", Map.of("parts", List.of(Map.of("text", instruccionSistema))));
        }
        body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        body.put("generationConfig", genConfig);

        try {
            JsonNode resp = rest.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String texto = extraerTexto(resp);
            if (texto == null || texto.isBlank()) {
                throw new IaException("El proveedor de IA devolvió una respuesta vacía.");
            }
            return texto.trim();
        } catch (IaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Fallo llamando al proveedor de IA (Gemini): {}", e.getMessage());
            throw new IaException("No se pudo contactar al proveedor de IA.", e);
        }
    }

    /** Concatena el texto de las partes del primer candidato de la respuesta de Gemini. */
    private String extraerTexto(JsonNode resp) {
        if (resp == null) {
            return null;
        }
        JsonNode parts = resp.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        parts.forEach(p -> sb.append(p.path("text").asText("")));
        return sb.toString();
    }
}
