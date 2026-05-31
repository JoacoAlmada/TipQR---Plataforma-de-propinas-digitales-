package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookPagoTest {

    @Test
    void builder_creaWebhookConCampos() {
        Pago pago = Pago.builder().build();
        WebhookPago wh = WebhookPago.builder()
                .pago(pago)
                .proveedor("MERCADO_PAGO")
                .tipoEvento("payment")
                .externalId("EXT-001")
                .payload("{\"id\":1}")
                .build();

        assertEquals("MERCADO_PAGO", wh.getProveedor());
        assertEquals("payment", wh.getTipoEvento());
        assertEquals("{\"id\":1}", wh.getPayload());
        assertEquals(pago, wh.getPago());
    }

    @Test
    void procesadoPorDefecto_esFalse() {
        WebhookPago wh = WebhookPago.builder().build();
        assertFalse(wh.getProcesado());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        WebhookPago wh = new WebhookPago();
        wh.setTipoEvento("refund");
        wh.setProcesado(true);

        assertEquals("refund", wh.getTipoEvento());
        assertTrue(wh.getProcesado());
    }
}
