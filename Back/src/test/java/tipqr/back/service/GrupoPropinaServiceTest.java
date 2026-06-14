package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.GrupoPropinaRequest;
import tipqr.back.dto.GrupoPropinaResponse;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.GrupoPropinaEmpleado;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpleadoRepository;
import tipqr.back.repository.GrupoPropinaEmpleadoRepository;
import tipqr.back.repository.GrupoPropinaRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrupoPropinaServiceTest {

    @Mock private GrupoPropinaRepository grupoRepository;
    @Mock private GrupoPropinaEmpleadoRepository miembroRepository;
    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private GrupoPropinaService grupoService;

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

    private GrupoPropinaRequest req(String nombre) {
        GrupoPropinaRequest r = new GrupoPropinaRequest();
        r.setNombre(nombre);
        r.setDescripcion("Equipo del turno noche");
        r.setTipoGrupo("Turno");
        r.setSucursalId(5L);
        return r;
    }

    @Test
    void crear_guardaGrupoEnLaSucursal() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(grupoRepository.existsByNombreIgnoreCaseAndSucursalId("Barra", 5L)).thenReturn(false);
        when(grupoRepository.save(any(GrupoPropina.class))).thenAnswer(inv -> inv.getArgument(0));

        GrupoPropinaResponse res = grupoService.crear(req("Barra"), EMAIL);

        assertEquals("Barra", res.getNombre());
        assertEquals("Turno", res.getTipoGrupo());
        assertEquals("Centro", res.getSucursalNombre());
        assertTrue(res.getEstado());
    }

    @Test
    void crear_nombreDuplicadoEnSucursal_lanza409() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(grupoRepository.existsByNombreIgnoreCaseAndSucursalId("Barra", 5L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> grupoService.crear(req("Barra"), EMAIL));
        verify(grupoRepository, never()).save(any());
    }

    @Test
    void crear_sucursalAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> grupoService.crear(req("Barra"), EMAIL));
        verify(grupoRepository, never()).save(any());
    }

    @Test
    void listar_porSucursal_devuelveGrupos() {
        GrupoPropina g = GrupoPropina.builder().id(7L).sucursal(sucursal).nombre("Barra").estado(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(grupoRepository.findBySucursalIdOrderByNombreAsc(5L)).thenReturn(List.of(g));

        List<GrupoPropinaResponse> res = grupoService.listar(EMAIL, 5L);

        assertEquals(1, res.size());
        assertEquals("Barra", res.get(0).getNombre());
    }

    @Test
    void obtenerPorId_grupoAjeno_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> grupoService.obtenerPorId(99L, EMAIL));
    }

    // ── Miembros del grupo ──────────────────────────────

    private GrupoPropina grupoEnSucursal() {
        return GrupoPropina.builder().id(3L).sucursal(sucursal).nombre("Barra").estado(true).build();
    }

    private Empleado empleadoEnSucursal(Long id, Sucursal suc) {
        return Empleado.builder().id(id).sucursal(suc).nombreVisible("Juan").build();
    }

    @Test
    void agregarEmpleado_mismaSucursal_loAgrega() {
        GrupoPropina grupo = grupoEnSucursal();
        Empleado emp = empleadoEnSucursal(8L, sucursal);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(3L, 1L)).thenReturn(Optional.of(grupo));
        when(empleadoRepository.findByIdAndSucursal_Empresa_Id(8L, 1L)).thenReturn(Optional.of(emp));
        when(miembroRepository.existsByGrupoPropinaIdAndEmpleadoId(3L, 8L)).thenReturn(false);

        grupoService.agregarEmpleado(3L, 8L, EMAIL);

        verify(miembroRepository).save(any(GrupoPropinaEmpleado.class));
    }

    @Test
    void agregarEmpleado_yaEnGrupo_lanza409() {
        GrupoPropina grupo = grupoEnSucursal();
        Empleado emp = empleadoEnSucursal(8L, sucursal);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(3L, 1L)).thenReturn(Optional.of(grupo));
        when(empleadoRepository.findByIdAndSucursal_Empresa_Id(8L, 1L)).thenReturn(Optional.of(emp));
        when(miembroRepository.existsByGrupoPropinaIdAndEmpleadoId(3L, 8L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> grupoService.agregarEmpleado(3L, 8L, EMAIL));
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void agregarEmpleado_otraSucursal_lanzaBadRequest() {
        GrupoPropina grupo = grupoEnSucursal();
        Sucursal otra = Sucursal.builder().id(9L).empresa(empresa).nombre("Norte").build();
        Empleado emp = empleadoEnSucursal(8L, otra);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(3L, 1L)).thenReturn(Optional.of(grupo));
        when(empleadoRepository.findByIdAndSucursal_Empresa_Id(8L, 1L)).thenReturn(Optional.of(emp));

        assertThrows(IllegalArgumentException.class, () -> grupoService.agregarEmpleado(3L, 8L, EMAIL));
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void removerEmpleado_noEnGrupo_lanza404() {
        GrupoPropina grupo = grupoEnSucursal();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(grupoRepository.findByIdAndSucursal_Empresa_Id(3L, 1L)).thenReturn(Optional.of(grupo));
        when(miembroRepository.findByGrupoPropinaIdAndEmpleadoId(3L, 8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> grupoService.removerEmpleado(3L, 8L, EMAIL));
        verify(miembroRepository, never()).delete(any());
    }
}
