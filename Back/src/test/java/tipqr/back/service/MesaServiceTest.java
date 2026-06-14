package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.MesaRequest;
import tipqr.back.dto.MesaResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Mesa;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.MesaRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock private MesaRepository mesaRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private MesaService mesaService;

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

    private MesaRequest req(int numero) {
        MesaRequest r = new MesaRequest();
        r.setNumero(numero);
        r.setDescripcion("Junto a la ventana");
        r.setSucursalId(5L);
        return r;
    }

    @Test
    void crear_guardaMesaEnLaSucursal() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(mesaRepository.existsBySucursalIdAndNumero(5L, 4)).thenReturn(false);
        when(mesaRepository.save(any(Mesa.class))).thenAnswer(inv -> inv.getArgument(0));

        MesaResponse res = mesaService.crear(req(4), EMAIL);

        assertEquals(4, res.getNumero());
        assertEquals("Centro", res.getSucursalNombre());
        assertTrue(res.getEstado());
    }

    @Test
    void crear_numeroDuplicadoEnSucursal_lanza409() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(mesaRepository.existsBySucursalIdAndNumero(5L, 4)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> mesaService.crear(req(4), EMAIL));
        verify(mesaRepository, never()).save(any());
    }

    @Test
    void crear_sucursalAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mesaService.crear(req(4), EMAIL));
        verify(mesaRepository, never()).save(any());
    }

    @Test
    void listar_porSucursal_devuelveMesas() {
        Mesa mesa = Mesa.builder().id(7L).sucursal(sucursal).numero(1).estado(true).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(sucursalRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(sucursal));
        when(mesaRepository.findBySucursalIdOrderByNumeroAsc(5L)).thenReturn(List.of(mesa));

        List<MesaResponse> res = mesaService.listar(EMAIL, 5L);

        assertEquals(1, res.size());
        assertEquals(1, res.get(0).getNumero());
    }

    @Test
    void obtenerPorId_mesaAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(mesaRepository.findByIdAndSucursal_Empresa_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mesaService.obtenerPorId(99L, EMAIL));
    }
}
