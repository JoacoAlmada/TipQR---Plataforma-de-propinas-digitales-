package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tipqr.back.dto.PagoIniciadoResponse;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.entity.Pago;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.OrdenPropinaRepository;
import tipqr.back.repository.PagoRepository;
import tipqr.back.repository.WebhookPagoRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock private OrdenPropinaRepository ordenRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private WebhookPagoRepository webhookRepository;
    @Mock private OrdenService ordenService;
    @Mock private DistribucionService distribucionService;
    @Mock private NotificacionService notificacionService;
    @Mock private MercadoPagoClient mpClient;
    @Mock private MercadoPagoSignatureValidator signatureValidator;
    @InjectMocks private PagoService pagoService;

    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagoService, "publicKey", "PUB-KEY");
        ReflectionTestUtils.setField(pagoService, "webhookUrl", "https://ngrok.test");
        ReflectionTestUtils.setField(pagoService, "frontendUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(pagoService, "validarFirma", true);
        sucursal = Sucursal.builder().id(5L).nombre("Centro").build();
    }

    private OrdenPropina orden(String codigo, EstadoOrden estado) {
        return OrdenPropina.builder().id(100L).codigo(codigo).sucursal(sucursal)
                .tipoPropina(TipoPropina.INDIVIDUAL).monto(new BigDecimal("2500"))
                .estado(estado).build();
    }

    // ── Iniciar pago ───────────────────────────────────────────

    @Test
    void iniciarPago_creaPreferenciaYMarcaPendiente() {
        OrdenPropina o = orden("ORD1", EstadoOrden.CREADA);
        when(ordenRepository.findByCodigo("ORD1")).thenReturn(Optional.of(o));
        when(pagoRepository.findByOrdenPropinaId(100L)).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mpClient.crearPreferencia(anyString(), any(), eq("ORD1"), anyString(), anyString()))
                .thenReturn(new MercadoPagoClient.PreferenciaCreada("PREF1", "https://mp/checkout"));

        PagoIniciadoResponse res = pagoService.iniciarPago("ORD1");

        assertEquals("PREF1", res.getPreferenceId());
        assertEquals("https://mp/checkout", res.getCheckoutUrl());
        assertEquals("PUB-KEY", res.getPublicKey());
        verify(ordenService).marcarPendientePago(o);
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    void iniciarPago_ordenEnEstadoFinal_lanza400() {
        when(ordenRepository.findByCodigo("ORD2")).thenReturn(Optional.of(orden("ORD2", EstadoOrden.PAGADA)));

        assertThrows(IllegalArgumentException.class, () -> pagoService.iniciarPago("ORD2"));
        verify(mpClient, never()).crearPreferencia(any(), any(), any(), any(), any());
    }

    @Test
    void iniciarPago_ordenInexistente_lanza404() {
        when(ordenRepository.findByCodigo("NOPE")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> pagoService.iniciarPago("NOPE"));
    }

    // ── Webhook ────────────────────────────────────────────────

    @Test
    void procesarWebhook_firmaInvalida_lanza400() {
        when(signatureValidator.esValida(any(), any(), any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> pagoService.procesarWebhook("payment", "123", "req", "sig", "{}"));
        verify(mpClient, never()).obtenerPago(any());
    }

    @Test
    void procesarWebhook_pagoAprobado_marcaPagada() {
        OrdenPropina o = orden("ORD3", EstadoOrden.PENDIENTE_PAGO);
        when(signatureValidator.esValida(any(), any(), any())).thenReturn(true);
        when(mpClient.obtenerPago("999")).thenReturn(
                new MercadoPagoClient.PagoMp("999", "approved", "ORD3", new BigDecimal("2500")));
        when(ordenRepository.findByCodigo("ORD3")).thenReturn(Optional.of(o));
        when(pagoRepository.findByOrdenPropinaId(100L)).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.procesarWebhook("payment", "999", "req", "sig", "{}");

        verify(ordenService).marcarPagada(o);
        verify(webhookRepository).save(any());
    }

    @Test
    void procesarWebhook_pagoRechazado_marcaRechazada() {
        OrdenPropina o = orden("ORD4", EstadoOrden.PENDIENTE_PAGO);
        when(signatureValidator.esValida(any(), any(), any())).thenReturn(true);
        when(mpClient.obtenerPago("888")).thenReturn(
                new MercadoPagoClient.PagoMp("888", "rejected", "ORD4", new BigDecimal("2500")));
        when(ordenRepository.findByCodigo("ORD4")).thenReturn(Optional.of(o));
        when(pagoRepository.findByOrdenPropinaId(100L)).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.procesarWebhook("payment", "888", "req", "sig", "{}");

        verify(ordenService).marcarRechazada(eq(o), anyString());
    }

    @Test
    void procesarWebhook_tipoNoPayment_ignora() {
        when(signatureValidator.esValida(any(), any(), any())).thenReturn(true);

        pagoService.procesarWebhook("merchant_order", "1", "req", "sig", "{}");

        verify(mpClient, never()).obtenerPago(any());
        verify(ordenService, never()).marcarPagada(any());
    }

    @Test
    void procesarWebhook_ordenYaPagada_noVuelveAConciliar() {
        OrdenPropina o = orden("ORD5", EstadoOrden.PAGADA);
        when(signatureValidator.esValida(any(), any(), any())).thenReturn(true);
        when(mpClient.obtenerPago("777")).thenReturn(
                new MercadoPagoClient.PagoMp("777", "approved", "ORD5", new BigDecimal("2500")));
        when(ordenRepository.findByCodigo("ORD5")).thenReturn(Optional.of(o));
        when(pagoRepository.findByOrdenPropinaId(100L)).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.procesarWebhook("payment", "777", "req", "sig", "{}");

        verify(ordenService, never()).marcarPagada(any());
        verify(webhookRepository).save(any());
    }

    // ── Conciliación desde el retorno (back_url, sin webhook) ───

    @Test
    void conciliarDesdeRetorno_pagoAprobado_marcaPagada() {
        OrdenPropina o = orden("ORD6", EstadoOrden.PENDIENTE_PAGO);
        when(mpClient.obtenerPago("555")).thenReturn(
                new MercadoPagoClient.PagoMp("555", "approved", "ORD6", new BigDecimal("2500")));
        when(ordenRepository.findByCodigo("ORD6")).thenReturn(Optional.of(o));
        when(pagoRepository.findByOrdenPropinaId(100L)).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.conciliarDesdeRetorno("555");

        verify(ordenService).marcarPagada(o);
        verify(webhookRepository).save(any());
    }

    @Test
    void conciliarDesdeRetorno_sinPaymentId_noHaceNada() {
        pagoService.conciliarDesdeRetorno(null);
        pagoService.conciliarDesdeRetorno("");

        verify(mpClient, never()).obtenerPago(any());
        verify(ordenService, never()).marcarPagada(any());
    }

    @Test
    void conciliarDesdeRetorno_errorDeMp_noPropaga() {
        when(mpClient.obtenerPago("err")).thenThrow(new RuntimeException("MP caído"));

        // No debe lanzar: el retorno es best-effort.
        pagoService.conciliarDesdeRetorno("err");

        verify(ordenService, never()).marcarPagada(any());
    }
}
