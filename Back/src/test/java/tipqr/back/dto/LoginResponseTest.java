package tipqr.back.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void constructor_asignaTodosLosCampos() {
        LoginResponse resp = new LoginResponse(
                "jwt-token-abc", "admin@tipqr.com", "DUENO", "Admin", "TipQR");

        assertEquals("jwt-token-abc", resp.getToken());
        assertEquals("admin@tipqr.com", resp.getEmail());
        assertEquals("DUENO", resp.getRol());
        assertEquals("Admin", resp.getNombre());
        assertEquals("TipQR", resp.getApellido());
    }

    @Test
    void constructor_conRolEmpleado_asignaCorrecto() {
        LoginResponse resp = new LoginResponse(
                "token-emp", "mozo@tipqr.com", "EMPLEADO", "Juan", "García");

        assertEquals("EMPLEADO", resp.getRol());
        assertEquals("mozo@tipqr.com", resp.getEmail());
    }

    @Test
    void token_noEsNulo() {
        LoginResponse resp = new LoginResponse("token", "email", "ROL", "nombre", "apellido");
        assertNotNull(resp.getToken());
        assertFalse(resp.getToken().isBlank());
    }
}
