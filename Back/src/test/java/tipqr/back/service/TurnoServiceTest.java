package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.TurnoAbrirRequest;
import tipqr.back.dto.TurnoResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Turno;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.GrupoPropinaRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.TurnoRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock private TurnoRepository turnoRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private GrupoPropinaRepository grupoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private TurnoService turnoService;

    private static final String EMAIL = "encargado@tipqr.com";
    private Empresa empresa;
    private Usuario usuario;
    private Sucursal sucursal;
    private GrupoPropina grupo;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(1L).nombre("Mi Bar").build();
        usuario = Usuario.builder().id(10L).nombre("Ana").apellido("Gómez").email(EMAIL).empresa(empresa).build();
        sucursal = Sucursal.builder().id(5L).empresa(empresa).nombre("Centro").build();
        grupo = GrupoPropina.builder().id(30L).sucursal(sucursal).nombre("Equipo Noche").build();
    }

    private TurnoAbrirRequest req() {
        TurnoAbrirRequest r = new TurnoAbrirRequest();
        r.setSucursalId(5L);
        r.setGrupoId(30L);
        r.setNombre("Noche");
        return r;
    }

    @Test
    void abrirTurno_creaTurnoActivoConElGrupo() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(30L, 1L)).thenReturn(Optional.of(grupo));
        when(turnoRepository.findBySucursalIdAndActivoTrue(5L)).thenReturn(Optional.empty());
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        TurnoResponse res = turnoService.abrirTurno(req(), EMAIL);

        assertTrue(res.getActivo());
        assertEquals(30L, res.getGrupoId());
        assertEquals(5L, res.getSucursalId());
        assertEquals("Equipo Noche", res.getGrupoNombre());
    }

    @Test
    void abrirTurno_cierraElTurnoAnterior() {
        Turno anterior = Turno.builder().id(99L).sucursal(sucursal).grupoPropina(grupo).activo(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(30L, 1L)).thenReturn(Optional.of(grupo));
        when(turnoRepository.findBySucursalIdAndActivoTrue(5L)).thenReturn(Optional.of(anterior));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        turnoService.abrirTurno(req(), EMAIL);

        assertFalse(anterior.getActivo());
        assertNotNull(anterior.getFechaCierre());
        verify(turnoRepository, times(2)).save(any(Turno.class)); // cierra el viejo + crea el nuevo
    }

    @Test
    void abrirTurno_grupoDeOtraSucursal_lanza400() {
        Sucursal otra = Sucursal.builder().id(99L).empresa(empresa).nombre("Otra").build();
        GrupoPropina grupoAjeno = GrupoPropina.builder().id(30L).sucursal(otra).nombre("X").build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(30L, 1L)).thenReturn(Optional.of(grupoAjeno));

        assertThrows(IllegalArgumentException.class, () -> turnoService.abrirTurno(req(), EMAIL));
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void abrirTurno_sucursalAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.abrirTurno(req(), EMAIL));
    }

    @Test
    void cerrarTurnoActivo_sinTurno_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(turnoRepository.findBySucursalIdAndActivoTrue(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.cerrarTurnoActivo(5L, EMAIL));
    }

    @Test
    void turnoActivo_devuelveElActivo() {
        Turno turno = Turno.builder().id(7L).sucursal(sucursal).grupoPropina(grupo).activo(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(turnoRepository.findBySucursalIdAndActivoTrue(5L)).thenReturn(Optional.of(turno));

        TurnoResponse res = turnoService.turnoActivo(5L, EMAIL);

        assertNotNull(res);
        assertEquals(30L, res.getGrupoId());
    }

    @Test
    void turnoActivo_sinTurno_devuelveNull() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(turnoRepository.findBySucursalIdAndActivoTrue(5L)).thenReturn(Optional.empty());

        assertNull(turnoService.turnoActivo(5L, EMAIL));
    }

    @Test
    void grupoActivo_devuelveElGrupoDelTurno() {
        Turno turno = Turno.builder().id(7L).sucursal(sucursal).grupoPropina(grupo).activo(true).build();
        when(turnoRepository.findBySucursalIdAndActivoTrue(5L)).thenReturn(Optional.of(turno));

        assertEquals(grupo, turnoService.grupoActivo(5L).orElseThrow());
    }
}
