package tipqr.back.entity;

import org.junit.jupiter.api.Test;
import tipqr.back.entity.enums.Rol;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void builder_creaUsuarioConCampos() {
        Usuario u = Usuario.builder()
                .nombre("Admin")
                .apellido("TipQR")
                .email("admin@tipqr.com")
                .password("hashed")
                .rol(Rol.DUENO)
                .build();

        assertEquals("Admin", u.getNombre());
        assertEquals("TipQR", u.getApellido());
        assertEquals("admin@tipqr.com", u.getEmail());
        assertEquals("hashed", u.getPassword());
        assertEquals(Rol.DUENO, u.getRol());
    }

    @Test
    void estadoPorDefecto_esTrue() {
        Usuario u = Usuario.builder().build();
        assertTrue(u.getEstado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        Usuario u = new Usuario();
        u.setEmail("test@tipqr.com");
        u.setRol(Rol.EMPLEADO);
        u.setNombre("Juan");

        assertEquals("test@tipqr.com", u.getEmail());
        assertEquals(Rol.EMPLEADO, u.getRol());
        assertEquals("Juan", u.getNombre());
    }

    @Test
    void todosLosRoles_sonValidos() {
        assertEquals(4, Rol.values().length);
        assertNotNull(Rol.valueOf("SUPERADMIN"));
        assertNotNull(Rol.valueOf("DUENO"));
        assertNotNull(Rol.valueOf("ENCARGADO"));
        assertNotNull(Rol.valueOf("EMPLEADO"));
    }
}
