package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.CrearNotificacionRequest;
import tipqr.back.entity.*;
import tipqr.back.entity.enums.OrigenNotificacion;
import tipqr.back.entity.enums.Rol;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private NotificacionDestinatarioRepository destinatarioRepository;
    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private NotificacionService notificacionService;

    private static final String EMISOR = "dueno@tipqr.com";
    private Empresa empresa;
    private Usuario dueno;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(1L).nombre("Mi Bar").build();
        dueno = Usuario.builder().id(10L).nombre("Ana").apellido("Gómez").email(EMISOR)
                .rol(Rol.DUENO).empresa(empresa).build();
        sucursal = Sucursal.builder().id(5L).empresa(empresa).nombre("Centro").build();
    }

    private Empleado empleadoCon(long id, String nombre) {
        Usuario u = Usuario.builder().id(100 + id).nombre(nombre).build();
        return Empleado.builder().id(id).nombreVisible(nombre).sucursal(sucursal).usuario(u).build();
    }

    private CrearNotificacionRequest req(Long sucursalId) {
        CrearNotificacionRequest r = new CrearNotificacionRequest();
        r.setTitulo("Reunión");
        r.setMensaje("Mañana a las 10");
        r.setSucursalId(sucursalId);
        return r;
    }

    @Test
    void enviar_aToda_LaEmpresa_creaUnDestinatarioPorEmpleado() {
        when(usuarioRepository.findByEmail(EMISOR)).thenReturn(Optional.of(dueno));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(i -> i.getArgument(0));
        when(empleadoRepository.findBySucursal_Empresa_IdOrderByNombreVisibleAsc(1L))
                .thenReturn(List.of(empleadoCon(1, "Sofia"), empleadoCon(2, "Tomas")));

        int enviados = notificacionService.enviar(req(null), EMISOR);

        assertEquals(2, enviados);
        verify(destinatarioRepository, times(2)).save(any(NotificacionDestinatario.class));
        ArgumentCaptor<Notificacion> cap = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(cap.capture());
        assertEquals(OrigenNotificacion.MANUAL, cap.getValue().getOrigen());
    }

    @Test
    void enviar_aSucursal_soloAEsaSucursal() {
        when(usuarioRepository.findByEmail(EMISOR)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(i -> i.getArgument(0));
        when(empleadoRepository.findBySucursalIdOrderByNombreVisibleAsc(5L))
                .thenReturn(List.of(empleadoCon(1, "Sofia")));

        int enviados = notificacionService.enviar(req(5L), EMISOR);

        assertEquals(1, enviados);
        verify(empleadoRepository, never()).findBySucursal_Empresa_IdOrderByNombreVisibleAsc(anyLong());
    }

    @Test
    void enviar_sucursalAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMISOR)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificacionService.enviar(req(5L), EMISOR));
    }

    @Test
    void notificarPropinaRecibida_creaNotificacionSistemaAlEmpleado() {
        Empleado emp = empleadoCon(1, "Sofia");
        when(usuarioRepository.findFirstByEmpresa_IdAndRolOrderByIdAsc(1L, Rol.DUENO))
                .thenReturn(Optional.of(dueno));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(i -> i.getArgument(0));

        notificacionService.notificarPropinaRecibida(emp, new BigDecimal("1500"), "(Mesa 4)");

        ArgumentCaptor<Notificacion> cap = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(cap.capture());
        assertEquals(OrigenNotificacion.SISTEMA, cap.getValue().getOrigen());
        assertTrue(cap.getValue().getMensaje().contains("1500"));
        verify(destinatarioRepository).save(any(NotificacionDestinatario.class));
    }

    @Test
    void notificarPropinaRecibida_empleadoSinUsuario_noHaceNada() {
        Empleado sinUsuario = Empleado.builder().id(9L).sucursal(sucursal).build();

        notificacionService.notificarPropinaRecibida(sinUsuario, new BigDecimal("100"), null);

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarLeida_actualizaElDestinatario() {
        Usuario u = Usuario.builder().id(10L).email(EMISOR).build();
        NotificacionDestinatario d = NotificacionDestinatario.builder().id(7L).usuario(u).leida(false).build();
        when(usuarioRepository.findByEmail(EMISOR)).thenReturn(Optional.of(u));
        when(destinatarioRepository.findByIdAndUsuarioId(7L, 10L)).thenReturn(Optional.of(d));
        when(destinatarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        notificacionService.marcarLeida(7L, EMISOR);

        assertTrue(d.getLeida());
        assertNotNull(d.getFechaLectura());
    }

    @Test
    void marcarLeida_ajena_lanza404() {
        Usuario u = Usuario.builder().id(10L).email(EMISOR).build();
        when(usuarioRepository.findByEmail(EMISOR)).thenReturn(Optional.of(u));
        when(destinatarioRepository.findByIdAndUsuarioId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificacionService.marcarLeida(99L, EMISOR));
    }
}
