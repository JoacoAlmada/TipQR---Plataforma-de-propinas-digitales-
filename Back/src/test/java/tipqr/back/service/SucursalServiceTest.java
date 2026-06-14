package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.SucursalRequest;
import tipqr.back.dto.SucursalResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpleadoRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmpleadoRepository empleadoRepository;
    @InjectMocks private SucursalService sucursalService;

    private static final String EMAIL = "dueno@tipqr.com";
    private Empresa empresa;
    private Usuario dueno;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(1L).nombre("Mi Bar").build();
        dueno = Usuario.builder().id(10L).email(EMAIL).empresa(empresa).build();
        sucursal = Sucursal.builder().id(5L).empresa(empresa).nombre("Centro").estado(true).build();
    }

    @Test
    void listar_devuelveSucursalesDeLaEmpresa() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByEmpresaIdOrderByNombreAsc(1L)).thenReturn(List.of(sucursal));

        List<SucursalResponse> res = sucursalService.listar(EMAIL);

        assertEquals(1, res.size());
        assertEquals("Centro", res.get(0).getNombre());
    }

    @Test
    void crear_guardaSucursalAsociadaALaEmpresa() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.existsByNombreIgnoreCaseAndEmpresaId("Norte", 1L)).thenReturn(false);
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        SucursalRequest req = new SucursalRequest();
        req.setNombre("Norte");
        req.setDireccion("Av. Siempre Viva 100");

        SucursalResponse res = sucursalService.crear(req, EMAIL);

        assertEquals("Norte", res.getNombre());
        assertTrue(res.getEstado());
    }

    @Test
    void crear_nombreDuplicado_lanza409() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.existsByNombreIgnoreCaseAndEmpresaId("Centro", 1L)).thenReturn(true);

        SucursalRequest req = new SucursalRequest();
        req.setNombre("Centro");

        assertThrows(DuplicateResourceException.class, () -> sucursalService.crear(req, EMAIL));
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_sucursalAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sucursalService.obtenerPorId(99L, EMAIL));
    }

    @Test
    void actualizar_sucursalAjena_lanza404YNoGuarda() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        SucursalRequest req = new SucursalRequest();
        req.setNombre("Hack");

        assertThrows(ResourceNotFoundException.class, () -> sucursalService.actualizar(99L, req, EMAIL));
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_desactivarConEmpleadosActivos_lanzaError() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(empleadoRepository.existsBySucursalIdAndEstadoTrue(5L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> sucursalService.cambiarEstado(5L, false, EMAIL));
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_desactivarSinEmpleados_funciona() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(empleadoRepository.existsBySucursalIdAndEstadoTrue(5L)).thenReturn(false);
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        SucursalResponse res = sucursalService.cambiarEstado(5L, false, EMAIL);

        assertFalse(res.getEstado());
    }
}
