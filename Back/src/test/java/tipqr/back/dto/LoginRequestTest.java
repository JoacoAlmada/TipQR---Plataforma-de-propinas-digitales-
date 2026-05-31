package tipqr.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void setterYGetter_funcionan() {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@tipqr.com");
        req.setPassword("tipqr2026");

        assertEquals("admin@tipqr.com", req.getEmail());
        assertEquals("tipqr2026", req.getPassword());
    }

    @Test
    void campoEmail_tieneAnotacionesDeValidacion() throws Exception {
        var field = LoginRequest.class.getDeclaredField("email");
        assertNotNull(field.getAnnotation(NotBlank.class), "email debe tener @NotBlank");
        assertNotNull(field.getAnnotation(Email.class), "email debe tener @Email");
    }

    @Test
    void campoPassword_tieneAnotacionNotBlank() throws Exception {
        var field = LoginRequest.class.getDeclaredField("password");
        assertNotNull(field.getAnnotation(NotBlank.class), "password debe tener @NotBlank");
    }

    @Test
    void emailVacio_noEsIgualAEmailValido() {
        LoginRequest req1 = new LoginRequest();
        LoginRequest req2 = new LoginRequest();
        req1.setEmail("");
        req2.setEmail("admin@tipqr.com");

        assertNotEquals(req1.getEmail(), req2.getEmail());
    }
}
