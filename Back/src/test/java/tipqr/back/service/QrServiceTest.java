package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tipqr.back.dto.QrResponse;
import tipqr.back.entity.CodigoQR;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Mesa;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.TipoDestinoQR;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.CodigoQRRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrServiceTest {

    @Mock private CodigoQRRepository qrRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private QrGenerator qrGenerator;
    @InjectMocks private QrService qrService;

    private static final String EMAIL = "dueno@tipqr.com";
    private Empresa empresa;
    private Usuario dueno;
    private Sucursal sucursal;
    private Mesa mesa;
    private Empleado empleado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(qrService, "frontendUrl", "http://localhost:4200");
        empresa = Empresa.builder().id(1L).nombre("Mi Bar").build();
        dueno = Usuario.builder().id(10L).email(EMAIL).empresa(empresa).build();
        sucursal = Sucursal.builder().id(5L).empresa(empresa).nombre("Centro").build();
        mesa = Mesa.builder().id(7L).sucursal(sucursal).numero(4).build();
        empleado = Empleado.builder().id(20L).sucursal(sucursal).nombreVisible("Juan").build();
    }

    // ── Alta automática ────────────────────────────────────────

    @Test
    void generarParaMesa_creaQrConDestinoMesaYUrlPublica() {
        when(qrRepository.findByMesaId(7L)).thenReturn(Optional.empty());
        when(qrRepository.existsByCodigo(anyString())).thenReturn(false);
        when(qrRepository.save(any(CodigoQR.class))).thenAnswer(inv -> inv.getArgument(0));

        CodigoQR qr = qrService.generarParaMesa(mesa);

        assertEquals(TipoDestinoQR.MESA, qr.getTipoDestino());
        assertEquals(mesa, qr.getMesa());
        assertNotNull(qr.getCodigo());
        assertTrue(qr.getUrl().startsWith("http://localhost:4200/propina/"));
        assertTrue(qr.getActivo());
    }

    @Test
    void generarParaMesa_yaTieneQr_devuelveElExistenteSinDuplicar() {
        CodigoQR existente = CodigoQR.builder().id(99L).codigo("VIEJO").mesa(mesa).build();
        when(qrRepository.findByMesaId(7L)).thenReturn(Optional.of(existente));

        CodigoQR qr = qrService.generarParaMesa(mesa);

        assertSame(existente, qr);
        verify(qrRepository, never()).save(any());
    }

    @Test
    void generarParaEmpleado_creaQrConDestinoEmpleado() {
        when(qrRepository.findByEmpleadoId(20L)).thenReturn(Optional.empty());
        when(qrRepository.existsByCodigo(anyString())).thenReturn(false);
        when(qrRepository.save(any(CodigoQR.class))).thenAnswer(inv -> inv.getArgument(0));

        CodigoQR qr = qrService.generarParaEmpleado(empleado);

        assertEquals(TipoDestinoQR.EMPLEADO, qr.getTipoDestino());
        assertEquals(empleado, qr.getEmpleado());
        assertNotNull(qr.getCodigo());
    }

    // ── Consulta ───────────────────────────────────────────────

    @Test
    void listar_porSucursal_devuelveQrs() {
        CodigoQR qr = CodigoQR.builder().id(1L).codigo("ABC").tipoDestino(TipoDestinoQR.MESA)
                .sucursal(sucursal).mesa(mesa).url("http://localhost:4200/propina/ABC").activo(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(qrRepository.findBySucursalIdOrderByFechaCreacionDesc(5L)).thenReturn(List.of(qr));

        List<QrResponse> res = qrService.listar(EMAIL, 5L);

        assertEquals(1, res.size());
        assertEquals("Mesa 4", res.get(0).getDestinoNombre());
        assertEquals("/api/qr/1/imagen", res.get(0).getImagenUrl());
    }

    @Test
    void obtenerImagenPng_devuelvePngNoVacio() {
        CodigoQR qr = CodigoQR.builder().id(1L).codigo("ABC").sucursal(sucursal)
                .url("http://localhost:4200/propina/ABC").build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(qrRepository.findByIdAndSucursal_Empresa_Id(1L, 1L)).thenReturn(Optional.of(qr));
        when(qrGenerator.generarPng("http://localhost:4200/propina/ABC")).thenReturn(new byte[]{1, 2, 3});

        byte[] png = qrService.obtenerImagenPng(1L, EMAIL);

        assertEquals(3, png.length);
    }

    @Test
    void obtenerImagenPng_qrAjeno_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(qrRepository.findByIdAndSucursal_Empresa_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> qrService.obtenerImagenPng(99L, EMAIL));
    }

    @Test
    void regenerar_cambiaElCodigoYLaUrl() {
        CodigoQR qr = CodigoQR.builder().id(1L).codigo("VIEJO").sucursal(sucursal)
                .url("http://localhost:4200/propina/VIEJO").activo(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(qrRepository.findByIdAndSucursal_Empresa_Id(1L, 1L)).thenReturn(Optional.of(qr));
        when(qrRepository.existsByCodigo(anyString())).thenReturn(false);
        when(qrRepository.save(any(CodigoQR.class))).thenAnswer(inv -> inv.getArgument(0));

        QrResponse res = qrService.regenerar(1L, EMAIL);

        assertNotEquals("VIEJO", res.getCodigo());
        assertTrue(res.getUrl().endsWith("/propina/" + res.getCodigo()));
    }
}
