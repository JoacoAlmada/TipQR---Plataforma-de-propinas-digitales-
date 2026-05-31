package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrupoPropinaTest {

    @Test
    void builder_creaGrupoConCampos() {
        Sucursal sucursal = Sucursal.builder().nombre("Centro").build();
        GrupoPropina g = GrupoPropina.builder()
                .sucursal(sucursal)
                .nombre("Turno Noche")
                .descripcion("Mozos turno noche")
                .tipoGrupo("TURNO")
                .build();

        assertEquals("Turno Noche", g.getNombre());
        assertEquals("Mozos turno noche", g.getDescripcion());
        assertEquals("TURNO", g.getTipoGrupo());
        assertEquals(sucursal, g.getSucursal());
    }

    @Test
    void estadoPorDefecto_esTrue() {
        GrupoPropina g = GrupoPropina.builder().nombre("Test").build();
        assertTrue(g.getEstado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        GrupoPropina g = new GrupoPropina();
        g.setNombre("Grupo Test");
        g.setTipoGrupo("GENERAL");

        assertEquals("Grupo Test", g.getNombre());
        assertEquals("GENERAL", g.getTipoGrupo());
    }
}
