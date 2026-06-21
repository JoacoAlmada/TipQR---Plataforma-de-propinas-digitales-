package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tipqr.back.dto.OrdenEstadoResponse;
import tipqr.back.entity.*;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.TipoEventoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.entity.CodigoQR;
import tipqr.back.entity.enums.TipoDestinoQR;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.CodigoQRRepository;
import tipqr.back.repository.EventoOrdenRepository;
import tipqr.back.repository.OrdenPropinaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock private OrdenPropinaRepository ordenRepository;
    @Mock private EventoOrdenRepository eventoRepository;
    @Mock private CodigoQRRepository qrRepository;
    @InjectMocks private OrdenService ordenService;

    private Empresa empresa;
    private Sucursal sucursal;
    private Empleado empleado;
    private GrupoPropina grupo;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ordenService, "vencimientoMinutos", 15L);
        empresa = Empresa.builder().id(1L).nombre("Mi Bar").build();
        sucursal = Sucursal.builder().id(5L).empresa(empresa).nombre("Centro").build();
        empleado = Empleado.builder().id(20L).sucursal(sucursal).build();
        grupo = GrupoPropina.builder().id(30L).sucursal(sucursal).build();
        lenient().when(ordenRepository.existsByCodigo(anyString())).thenReturn(false);
        lenient().when(ordenRepository.save(any(OrdenPropina.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── Creación ───────────────────────────────────────────────

    @Test
    void crearOrden_individual_quedaCreadaConCodigoVencimientoYEvento() {
        OrdenPropina orden = ordenService.crearOrden(
                sucursal, TipoPropina.INDIVIDUAL, new BigDecimal("2500.00"), null, empleado, null);

        assertEquals(EstadoOrden.CREADA, orden.getEstado());
        assertNotNull(orden.getCodigo());
        assertEquals(8, orden.getCodigo().length());
        assertNotNull(orden.getFechaExpiracion());
        assertTrue(orden.getFechaExpiracion().isAfter(LocalDateTime.now()));
        verify(eventoRepository).save(argThat(e -> e.getTipoEvento() == TipoEventoOrden.ORDEN_CREADA));
    }

    @Test
    void crearOrden_montoCeroOnegativo_lanza400() {
        assertThrows(IllegalArgumentException.class, () -> ordenService.crearOrden(
                sucursal, TipoPropina.INDIVIDUAL, BigDecimal.ZERO, null, empleado, null));
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void crearOrden_individualSinEmpleado_lanza400() {
        assertThrows(IllegalArgumentException.class, () -> ordenService.crearOrden(
                sucursal, TipoPropina.INDIVIDUAL, new BigDecimal("1000"), null, null, null));
    }

    @Test
    void crearOrden_grupalSinGrupo_lanza400() {
        assertThrows(IllegalArgumentException.class, () -> ordenService.crearOrden(
                sucursal, TipoPropina.GRUPAL, new BigDecimal("1000"), null, null, null));
    }

    @Test
    void crearOrden_sinSucursal_lanza400() {
        assertThrows(IllegalArgumentException.class, () -> ordenService.crearOrden(
                null, TipoPropina.INDIVIDUAL, new BigDecimal("1000"), null, empleado, null));
    }

    // ── Transiciones ───────────────────────────────────────────

    @Test
    void marcarPendientePago_dejaPendienteYRegistraEvento() {
        OrdenPropina orden = ordenPendiente(EstadoOrden.CREADA);

        ordenService.marcarPendientePago(orden);

        assertEquals(EstadoOrden.PENDIENTE_PAGO, orden.getEstado());
        verify(eventoRepository).save(argThat(e -> e.getTipoEvento() == TipoEventoOrden.PREFERENCIA_MP_GENERADA));
    }

    @Test
    void marcarPagada_seteaFechaPagoYEstado() {
        OrdenPropina orden = ordenPendiente(EstadoOrden.PENDIENTE_PAGO);

        ordenService.marcarPagada(orden);

        assertEquals(EstadoOrden.PAGADA, orden.getEstado());
        assertNotNull(orden.getFechaPago());
        verify(eventoRepository).save(argThat(e -> e.getTipoEvento() == TipoEventoOrden.ORDEN_PAGADA));
    }

    @Test
    void marcarPagada_sobreOrdenYaPagada_lanza400() {
        OrdenPropina orden = ordenPendiente(EstadoOrden.PAGADA);

        assertThrows(IllegalArgumentException.class, () -> ordenService.marcarPagada(orden));
    }

    @Test
    void cancelar_sobreOrdenExpirada_lanza400() {
        OrdenPropina orden = ordenPendiente(EstadoOrden.EXPIRADA);

        assertThrows(IllegalArgumentException.class, () -> ordenService.cancelar(orden, "tarde"));
    }

    // ── Expiración ─────────────────────────────────────────────

    @Test
    void expirarOrdenesVencidas_pasaAExpiradaYRegistraEvento() {
        OrdenPropina vencida = ordenPendiente(EstadoOrden.PENDIENTE_PAGO);
        vencida.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));
        when(ordenRepository.findByEstadoInAndFechaExpiracionBefore(anyList(), any()))
                .thenReturn(List.of(vencida));

        ordenService.expirarOrdenesVencidas();

        assertEquals(EstadoOrden.EXPIRADA, vencida.getEstado());
        verify(eventoRepository).save(argThat(e -> e.getTipoEvento() == TipoEventoOrden.ORDEN_EXPIRADA));
    }

    @Test
    void expirarOrdenesVencidas_sinVencidas_noHaceNada() {
        when(ordenRepository.findByEstadoInAndFechaExpiracionBefore(anyList(), any()))
                .thenReturn(List.of());

        ordenService.expirarOrdenesVencidas();

        verify(eventoRepository, never()).save(any());
        verify(ordenRepository, never()).save(any());
    }

    // ── Consulta pública ───────────────────────────────────────

    @Test
    void consultarEstadoPublico_devuelveEstado() {
        OrdenPropina orden = ordenPendiente(EstadoOrden.PENDIENTE_PAGO);
        orden.setCodigo("ABC12345");
        when(ordenRepository.findByCodigo("ABC12345")).thenReturn(Optional.of(orden));

        OrdenEstadoResponse res = ordenService.consultarEstadoPublico("ABC12345");

        assertEquals("ABC12345", res.getCodigo());
        assertEquals("PENDIENTE_PAGO", res.getEstado());
        assertEquals("Centro", res.getSucursalNombre());
    }

    @Test
    void consultarEstadoPublico_codigoInexistente_lanza404() {
        when(ordenRepository.findByCodigo("NOPE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordenService.consultarEstadoPublico("NOPE"));
    }

    // ── Crear desde QR (pantalla pública) ──────────────────────

    @Test
    void crearDesdeQr_qrDeEmpleado_creaOrdenIndividual() {
        CodigoQR qr = CodigoQR.builder().codigo("QR1").activo(true)
                .tipoDestino(TipoDestinoQR.EMPLEADO).sucursal(sucursal).empleado(empleado).build();
        when(qrRepository.findByCodigo("QR1")).thenReturn(Optional.of(qr));

        OrdenPropina orden = ordenService.crearDesdeQr("QR1", new BigDecimal("2000"));

        assertEquals(TipoPropina.INDIVIDUAL, orden.getTipoPropina());
        assertEquals(empleado, orden.getEmpleado());
        assertEquals(EstadoOrden.CREADA, orden.getEstado());
    }

    @Test
    void crearDesdeQr_qrDeMesa_creaOrdenIndividualConMesa() {
        Mesa mesa = Mesa.builder().id(8L).sucursal(sucursal).numero(3).build();
        CodigoQR qr = CodigoQR.builder().codigo("QR2").activo(true)
                .tipoDestino(TipoDestinoQR.MESA).sucursal(sucursal).mesa(mesa).build();
        when(qrRepository.findByCodigo("QR2")).thenReturn(Optional.of(qr));

        OrdenPropina orden = ordenService.crearDesdeQr("QR2", new BigDecimal("1000"));

        assertEquals(TipoPropina.INDIVIDUAL, orden.getTipoPropina());
        assertEquals(mesa, orden.getMesa());
        assertNull(orden.getEmpleado());
    }

    @Test
    void crearDesdeQr_qrInexistente_lanza404() {
        when(qrRepository.findByCodigo("NOPE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ordenService.crearDesdeQr("NOPE", new BigDecimal("1000")));
    }

    @Test
    void crearDesdeQr_qrInactivo_lanza404() {
        CodigoQR qr = CodigoQR.builder().codigo("QR3").activo(false)
                .tipoDestino(TipoDestinoQR.EMPLEADO).sucursal(sucursal).empleado(empleado).build();
        when(qrRepository.findByCodigo("QR3")).thenReturn(Optional.of(qr));

        assertThrows(ResourceNotFoundException.class,
                () -> ordenService.crearDesdeQr("QR3", new BigDecimal("1000")));
    }

    private OrdenPropina ordenPendiente(EstadoOrden estado) {
        return OrdenPropina.builder()
                .id(100L)
                .codigo("ABC12345")
                .sucursal(sucursal)
                .tipoPropina(TipoPropina.INDIVIDUAL)
                .empleado(empleado)
                .monto(new BigDecimal("2500.00"))
                .estado(estado)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();
    }
}
