package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrupoPropinaEmpleadoTest {

    @Test
    void builder_creaRelacionConCampos() {
        GrupoPropina grupo = GrupoPropina.builder().nombre("Grupo A").build();
        Empleado empleado = Empleado.builder().nombreVisible("Juan").build();

        GrupoPropinaEmpleado gpe = GrupoPropinaEmpleado.builder()
                .grupoPropina(grupo)
                .empleado(empleado)
                .porcentajeDistribucion(33.33)
                .build();

        assertEquals(grupo, gpe.getGrupoPropina());
        assertEquals(empleado, gpe.getEmpleado());
        assertEquals(33.33, gpe.getPorcentajeDistribucion());
    }

    @Test
    void activoPorDefecto_esTrue() {
        GrupoPropinaEmpleado gpe = GrupoPropinaEmpleado.builder().build();
        assertTrue(gpe.getActivo());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        GrupoPropinaEmpleado gpe = new GrupoPropinaEmpleado();
        gpe.setPorcentajeDistribucion(50.0);
        gpe.setActivo(false);

        assertEquals(50.0, gpe.getPorcentajeDistribucion());
        assertFalse(gpe.getActivo());
    }
}
