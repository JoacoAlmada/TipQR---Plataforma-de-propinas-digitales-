package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tipqr.back.dto.EmpleadoRequest;
import tipqr.back.dto.EmpleadoResponse;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.Rol;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpleadoRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private QrService qrService;
    @InjectMocks private EmpleadoService empleadoService;

    private static final String EMAIL = "dueno@tipqr.com";
    private Empresa empresa;
    private Usuario dueno;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(1L).nombre("Mi Bar").build();
        dueno = Usuario.builder().id(10L).email(EMAIL).empresa(empresa).build();
        sucursal = Sucursal.builder().id(5L).empresa(empresa).nombre("Centro").build();
    }

    private EmpleadoRequest req() {
        EmpleadoRequest r = new EmpleadoRequest();
        r.setNombreVisible("Juan");
        r.setApellido("Pérez");
        r.setEmail("juan@mibar.com");
        r.setPuesto("Mozo");
        r.setSucursalId(5L);
        return r;
    }

    @Test
    void crear_generaUsuarioEmpleadoYDevuelvePasswordTemporal() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(usuarioRepository.existsByEmail("juan@mibar.com")).thenReturn(false);
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpleadoResponse res = empleadoService.crear(req(), EMAIL);

        assertEquals("Juan", res.getNombreVisible());
        assertEquals("juan@mibar.com", res.getEmail());
        assertEquals("Centro", res.getSucursalNombre());
        assertNotNull(res.getPasswordTemporal());
        assertFalse(res.getPasswordTemporal().isBlank());
        verify(emailService).enviarBienvenidaEmpleado(eq("juan@mibar.com"), anyString(), anyString());
    }

    @Test
    void crear_emailDuplicado_lanza409() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(usuarioRepository.existsByEmail("juan@mibar.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> empleadoService.crear(req(), EMAIL));
        verify(empleadoRepository, never()).save(any());
    }

    @Test
    void crear_sucursalAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(usuarioRepository.existsByEmail("juan@mibar.com")).thenReturn(false);
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empleadoService.crear(req(), EMAIL));
        verify(empleadoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_empleadoAjeno_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(empleadoRepository.findByIdAndSucursal_Empresa_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empleadoService.obtenerPorId(99L, EMAIL));
    }

    @Test
    void marcarEncargado_cambiaRolDelUsuarioAEncargado() {
        Usuario uEmp = Usuario.builder().id(20L).email("juan@mibar.com").rol(Rol.EMPLEADO).build();
        Empleado empleado = Empleado.builder().id(7L).usuario(uEmp).sucursal(sucursal)
                .nombreVisible("Juan").estado(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(empleadoRepository.findByIdAndSucursal_Empresa_Id(7L, 1L)).thenReturn(Optional.of(empleado));

        EmpleadoResponse res = empleadoService.marcarEncargado(7L, true, EMAIL);

        assertEquals(Rol.ENCARGADO, uEmp.getRol());
        assertTrue(res.getEsEncargado());
        verify(usuarioRepository).save(uEmp);
    }

    @Test
    void cambiarEstado_desactiva_tambienAlUsuario() {
        Usuario uEmp = Usuario.builder().id(20L).email("juan@mibar.com").estado(true).build();
        Empleado empleado = Empleado.builder().id(7L).usuario(uEmp).sucursal(sucursal)
                .nombreVisible("Juan").estado(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(empleadoRepository.findByIdAndSucursal_Empresa_Id(7L, 1L)).thenReturn(Optional.of(empleado));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpleadoResponse res = empleadoService.cambiarEstado(7L, false, EMAIL);

        assertFalse(res.getEstado());
        assertFalse(uEmp.getEstado());
        verify(usuarioRepository).save(uEmp);
    }
}
