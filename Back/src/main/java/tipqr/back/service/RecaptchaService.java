package tipqr.back.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Verifica el token de Google reCAPTCHA v2 ("no soy un robot").
 */
@Service
@Slf4j
public class RecaptchaService {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestClient restClient = RestClient.create();

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    @Value("${recaptcha.enabled:true}")
    private boolean enabled;

    /**
     * @return true si el captcha es válido (o si está deshabilitado por config).
     */
    @SuppressWarnings("unchecked")
    public boolean esValido(String token) {
        if (!enabled) {
            return true;
        }
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("secret", secretKey);
            form.add("response", token);

            Map<String, Object> respuesta = restClient.post()
                    .uri(VERIFY_URL)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            boolean success = respuesta != null && Boolean.TRUE.equals(respuesta.get("success"));
            if (!success) {
                log.warn("reCAPTCHA inválido: {}", respuesta);
            }
            return success;
        } catch (Exception e) {
            log.error("Error verificando reCAPTCHA: {}", e.getMessage());
            return false;
        }
    }
}
