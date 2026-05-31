package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmpleadoTest {

    @Test
    void builder_creaEmpleadoConCampos() {
        Usuario usuario = Usuario.builder().nombre("Juan").build();
        Sucursal sucursal = Sucursal.builder().nombre("Centro").build();

        Empleado e = Empleado.builder()
                .usuario(usuario)
                .sucursal(sucursal)
                .nombreVisible("Juan García")
                .puesto("Mozo")
                .build();

        assertEquals("Juan García", e.getNombreVisible());
        assertEquals("Mozo", e.getPuesto());
        assertEquals(usuario, e.getUsuario());
        assertEquals(sucursal, e.getSucursal());
    }

    @Test
    void estadoPorDefecto_esTrue() {
        Empleado e = Empleado.builder().nombreVisible("Test").build();
        assertTrue(e.getEstado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        Empleado e = new Empleado();
        e.setNombreVisible("Pedro López");
        e.setPuesto("Bartender");

        assertEquals("Pedro López", e.getNombreVisible());
        assertEquals("Bartender", e.getPuesto());
    }
}
