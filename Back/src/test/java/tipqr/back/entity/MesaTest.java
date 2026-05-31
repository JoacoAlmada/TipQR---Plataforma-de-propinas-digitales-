package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MesaTest {

    @Test
    void builder_creaMesaConCampos() {
        Sucursal sucursal = Sucursal.builder().nombre("Centro").build();
        Mesa m = Mesa.builder()
                .sucursal(sucursal)
                .numero(5)
                .descripcion("Ventana")
                .build();

        assertEquals(5, m.getNumero());
        assertEquals("Ventana", m.getDescripcion());
        assertEquals(sucursal, m.getSucursal());
    }

    @Test
    void estadoPorDefecto_esTrue() {
        Mesa m = Mesa.builder().numero(1).build();
        assertTrue(m.getEstado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        Mesa m = new Mesa();
        m.setNumero(10);
        m.setDescripcion("Terraza");

        assertEquals(10, m.getNumero());
        assertEquals("Terraza", m.getDescripcion());
    }
}
