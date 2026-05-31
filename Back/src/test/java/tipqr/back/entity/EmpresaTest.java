package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaTest {

    @Test
    void builder_creaEmpresaConCampos() {
        Empresa e = Empresa.builder()
                .nombre("Restó Demo")
                .rubro("Gastronomía")
                .cuit("30-12345678-9")
                .emailContacto("demo@tipqr.com")
                .telefono("3513000000")
                .build();

        assertEquals("Restó Demo", e.getNombre());
        assertEquals("Gastronomía", e.getRubro());
        assertEquals("30-12345678-9", e.getCuit());
        assertEquals("demo@tipqr.com", e.getEmailContacto());
        assertEquals("3513000000", e.getTelefono());
    }

    @Test
    void estadoPorDefecto_esTrue() {
        Empresa e = Empresa.builder().nombre("Test").build();
        assertTrue(e.getEstado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        Empresa e = new Empresa();
        e.setNombre("Nuevo Resto");
        e.setRubro("Bar");
        e.setCuit("20-99999999-1");

        assertEquals("Nuevo Resto", e.getNombre());
        assertEquals("Bar", e.getRubro());
        assertEquals("20-99999999-1", e.getCuit());
    }

    @Test
    void setEstado_actualizaValor() {
        Empresa e = Empresa.builder().nombre("Test").build();
        e.setEstado(false);
        assertFalse(e.getEstado());
    }
}
