package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SucursalTest {

    @Test
    void builder_creaSucursalConCampos() {
        Empresa empresa = Empresa.builder().nombre("Empresa Test").build();
        Sucursal s = Sucursal.builder()
                .empresa(empresa)
                .nombre("Sucursal Centro")
                .direccion("Av. Colón 123")
                .telefono("3514444444")
                .build();

        assertEquals("Sucursal Centro", s.getNombre());
        assertEquals("Av. Colón 123", s.getDireccion());
        assertEquals(empresa, s.getEmpresa());
    }

    @Test
    void estadoPorDefecto_esTrue() {
        Sucursal s = Sucursal.builder().nombre("Test").build();
        assertTrue(s.getEstado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        Sucursal s = new Sucursal();
        s.setNombre("Nueva Sucursal");
        s.setDireccion("Calle Falsa 123");

        assertEquals("Nueva Sucursal", s.getNombre());
        assertEquals("Calle Falsa 123", s.getDireccion());
    }
}
