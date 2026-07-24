package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Envía el email de verificación con el link para activar la cuenta.
     */
    public void enviarVerificacion(String destinatario, String nombre, String token) {
        String link = backendUrl + "/api/registro/verificar?token=" + token;
        String html = """
                <div style="font-family:Inter,Arial,sans-serif;max-width:520px;margin:auto;padding:24px;color:#040b15">
                  <h2 style="color:#640000">TipQR</h2>
                  <p>Hola %s,</p>
                  <p>Gracias por registrar tu comercio en TipQR. Para continuar con el alta,
                     confirmá tu dirección de email haciendo clic en el botón:</p>
                  <p style="text-align:center;margin:28px 0">
                    <a href="%s" style="background:#640000;color:#fff;text-decoration:none;
                       padding:12px 24px;border-radius:10px;font-weight:600">Validar mi email</a>
                  </p>
                  <p style="color:#6b6266;font-size:13px">Si el botón no funciona, copiá este link:<br>%s</p>
                  <p style="color:#6b6266;font-size:13px">Si no fuiste vos, ignorá este mensaje.</p>
                </div>
                """.formatted(nombre, link, link);

        enviar(destinatario, "Validá tu email — TipQR", html, "verificación");
    }

    /**
     * Notifica al dueño el resultado de la validación de su cuenta. Si fue rechazada, incluye un
     * botón para retomar el formulario, corregir y reenviar (usando el token de registro).
     */
    public void enviarResultadoValidacion(String destinatario, String nombre, boolean aprobada,
                                          String motivo, String tokenRegistro) {
        String titulo = aprobada ? "¡Tu cuenta fue aprobada!" : "Tu solicitud fue rechazada";
        String cuerpo = aprobada
                ? "Ya podés ingresar a TipQR y empezar a administrar tu comercio."
                : "Revisamos tu solicitud y no pudimos aprobarla." +
                  (motivo != null && !motivo.isBlank() ? " <strong>Motivo:</strong> " + motivo : "");
        String color = aprobada ? "#16a34a" : "#dc2626";

        String accion = "";
        if (!aprobada && tokenRegistro != null) {
            String link = frontendUrl + "/registro/corregir?token=" + tokenRegistro;
            accion = """
                    <p>Podés corregir lo que haga falta y reenviar tu solicitud:</p>
                    <p style="text-align:center;margin:28px 0">
                      <a href="%s" style="background:#640000;color:#fff;text-decoration:none;
                         padding:12px 24px;border-radius:10px;font-weight:600">Corregir y reenviar</a>
                    </p>
                    <p style="color:#6b6266;font-size:13px">Si el botón no funciona, copiá este link:<br>%s</p>
                    """.formatted(link, link);
        }

        String html = """
                <div style="font-family:Inter,Arial,sans-serif;max-width:520px;margin:auto;padding:24px;color:#040b15">
                  <h2 style="color:#640000">TipQR</h2>
                  <p>Hola %s,</p>
                  <h3 style="color:%s">%s</h3>
                  <p>%s</p>
                  %s
                </div>
                """.formatted(nombre, color, titulo, cuerpo, accion);

        enviar(destinatario, titulo + " — TipQR", html, "resultado de validación");
    }

    /**
     * Da la bienvenida a un empleado recién creado con su contraseña temporal.
     */
    public void enviarBienvenidaEmpleado(String destinatario, String nombre, String passwordTemporal) {
        String html = """
                <div style="font-family:Inter,Arial,sans-serif;max-width:520px;margin:auto;padding:24px;color:#040b15">
                  <h2 style="color:#640000">TipQR</h2>
                  <p>Hola %s,</p>
                  <p>Se creó tu cuenta de empleado en TipQR. Ya podés ingresar con:</p>
                  <p style="background:#f3ece3;padding:12px 16px;border-radius:10px">
                    <strong>Email:</strong> %s<br>
                    <strong>Contraseña temporal:</strong> %s
                  </p>
                  <p style="color:#6b6266;font-size:13px">Te recomendamos cambiarla al ingresar.</p>
                </div>
                """.formatted(nombre, destinatario, passwordTemporal);

        enviar(destinatario, "Tu cuenta de empleado — TipQR", html, "bienvenida de empleado");
    }

    private void enviar(String destinatario, String asunto, String html, String concepto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email de {} enviado a {}", concepto, destinatario);
        } catch (Exception e) {
            log.error("Error enviando email de {} a {}: {}", concepto, destinatario, e.getMessage());
            throw new IllegalStateException("No se pudo enviar el email");
        }
    }
}
