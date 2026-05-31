package tipqr.back.entity;

import org.junit.jupiter.api.Test;
import tipqr.back.entity.enums.TipoEventoOrden;

import static org.junit.jupiter.api.Assertions.*;

class EventoOrdenTest {

    @Test
    void builder_creaEventoConCampos() {
        OrdenPropina orden = OrdenPropina.builder().codigo("ORD-001").build();
        EventoOrden evento = EventoOrden.builder()
                .ordenPropina(orden)
                .tipoEvento(TipoEventoOrden.ORDEN_CREADA)
                .descripcion("Orden creada por escaneo QR")
                .build();

        assertEquals(TipoEventoOrden.ORDEN_CREADA, evento.getTipoEvento());
        assertEquals("Orden creada por escaneo QR", evento.getDescripcion());
        assertEquals(orden, evento.getOrdenPropina());
    }

    @Test
    void tiposEventoOrden_sonValidos() {
        assertEquals(6, TipoEventoOrden.values().length);
        assertNotNull(TipoEventoOrden.valueOf("ORDEN_CREADA"));
        assertNotNull(TipoEventoOrden.valueOf("PAGO_CONFIRMADO"));
        assertNotNull(TipoEventoOrden.valueOf("ORDEN_PAGADA"));
        assertNotNull(TipoEventoOrden.valueOf("ORDEN_EXPIRADA"));
        assertNotNull(TipoEventoOrden.valueOf("DISTRIBUCION_GENERADA"));
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        EventoOrden evento = new EventoOrden();
        evento.setTipoEvento(TipoEventoOrden.ORDEN_PAGADA);
        evento.setDescripcion("Pago confirmado");

        assertEquals(TipoEventoOrden.ORDEN_PAGADA, evento.getTipoEvento());
        assertEquals("Pago confirmado", evento.getDescripcion());
    }
}
