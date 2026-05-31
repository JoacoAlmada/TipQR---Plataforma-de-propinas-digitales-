package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DistribucionPropinaTest {

    @Test
    void builder_creaDistribucionConCampos() {
        OrdenPropina orden = OrdenPropina.builder().codigo("ORD-001").build();
        Empleado empleado = Empleado.builder().nombreVisible("Juan").build();

        DistribucionPropina d = DistribucionPropina.builder()
                .ordenPropina(orden)
                .empleado(empleado)
                .montoAsignado(new BigDecimal("166.67"))
                .porcentaje(33.33)
                .criterio("EQUITATIVO")
                .build();

        assertEquals(new BigDecimal("166.67"), d.getMontoAsignado());
        assertEquals(33.33, d.getPorcentaje());
        assertEquals("EQUITATIVO", d.getCriterio());
        assertEquals(orden, d.getOrdenPropina());
        assertEquals(empleado, d.getEmpleado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        DistribucionPropina d = new DistribucionPropina();
        d.setMontoAsignado(new BigDecimal("250.00"));
        d.setPorcentaje(50.0);

        assertEquals(new BigDecimal("250.00"), d.getMontoAsignado());
        assertEquals(50.0, d.getPorcentaje());
    }
}
