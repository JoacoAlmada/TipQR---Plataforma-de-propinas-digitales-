package tipqr.back.entity;

import org.junit.jupiter.api.Test;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.TipoPropina;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrdenPropinaTest {

    @Test
    void builder_creaOrdenConCampos() {
        Sucursal sucursal = Sucursal.builder().nombre("Centro").build();
        OrdenPropina orden = OrdenPropina.builder()
                .codigo("ORD-001")
                .sucursal(sucursal)
                .tipoPropina(TipoPropina.INDIVIDUAL)
                .monto(new BigDecimal("500.00"))
                .estado(EstadoOrden.CREADA)
                .build();

        assertEquals("ORD-001", orden.getCodigo());
        assertEquals(TipoPropina.INDIVIDUAL, orden.getTipoPropina());
        assertEquals(new BigDecimal("500.00"), orden.getMonto());
        assertEquals(EstadoOrden.CREADA, orden.getEstado());
    }

    @Test
    void estadosOrden_sonValidos() {
        assertEquals(6, EstadoOrden.values().length);
        assertNotNull(EstadoOrden.valueOf("CREADA"));
        assertNotNull(EstadoOrden.valueOf("PENDIENTE_PAGO"));
        assertNotNull(EstadoOrden.valueOf("PAGADA"));
        assertNotNull(EstadoOrden.valueOf("RECHAZADA"));
        assertNotNull(EstadoOrden.valueOf("CANCELADA"));
        assertNotNull(EstadoOrden.valueOf("EXPIRADA"));
    }

    @Test
    void tiposPropina_sonValidos() {
        assertEquals(2, TipoPropina.values().length);
        assertNotNull(TipoPropina.valueOf("INDIVIDUAL"));
        assertNotNull(TipoPropina.valueOf("GRUPAL"));
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        OrdenPropina orden = new OrdenPropina();
        orden.setCodigo("ORD-002");
        orden.setMonto(new BigDecimal("200.00"));
        orden.setEstado(EstadoOrden.PAGADA);

        assertEquals("ORD-002", orden.getCodigo());
        assertEquals(EstadoOrden.PAGADA, orden.getEstado());
    }
}
