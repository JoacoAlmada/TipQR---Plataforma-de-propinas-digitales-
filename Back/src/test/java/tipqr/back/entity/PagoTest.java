package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagoTest {

    @Test
    void builder_creaPagoConCampos() {
        OrdenPropina orden = OrdenPropina.builder().codigo("ORD-001").build();
        Pago pago = Pago.builder()
                .ordenPropina(orden)
                .externalPaymentId("MP-12345")
                .preferenceId("PREF-001")
                .monto(new BigDecimal("500.00"))
                .estadoProveedor("approved")
                .build();

        assertEquals("MP-12345", pago.getExternalPaymentId());
        assertEquals("PREF-001", pago.getPreferenceId());
        assertEquals(new BigDecimal("500.00"), pago.getMonto());
        assertEquals("approved", pago.getEstadoProveedor());
        assertEquals(orden, pago.getOrdenPropina());
    }

    @Test
    void proveedorPorDefecto_esMercadoPago() {
        Pago pago = Pago.builder().build();
        assertEquals("MERCADO_PAGO", pago.getProveedor());
    }

    @Test
    void monedaPorDefecto_esARS() {
        Pago pago = Pago.builder().build();
        assertEquals("ARS", pago.getMoneda());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        Pago pago = new Pago();
        pago.setExternalPaymentId("EXT-999");
        pago.setEstadoProveedor("pending");

        assertEquals("EXT-999", pago.getExternalPaymentId());
        assertEquals("pending", pago.getEstadoProveedor());
    }
}
