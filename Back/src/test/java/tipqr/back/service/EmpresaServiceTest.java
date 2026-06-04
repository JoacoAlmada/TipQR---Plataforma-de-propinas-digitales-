package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.EmpresaRequest;
import tipqr.back.dto.EmpresaResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpresaRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private EmpresaService empresaService;

    private Empresa empresaPropia;
    private Usuario dueno;

    private static final String EMAIL = "dueno@tipqr.com";

    @BeforeEach
    void setUp() {
        empresaPropia = Empresa.builder().id(1L).nombre("Mi Bar").estado(true).build();
        dueno = Usuario.builder().id(10L).email(EMAIL).empresa(empresaPropia).build();
    }

    @Test
    void listar_devuelveSoloLaEmpresaDelUsuario() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));

        List<EmpresaResponse> resultado = empresaService.listar(EMAIL);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Mi Bar", resultado.get(0).getNombre());
    }

    @Test
    void miEmpresa_devuelveLaEmpresaAsociada() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));

        EmpresaResponse resultado = empresaService.miEmpresa(EMAIL);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void miEmpresa_usuarioSinEmpresa_lanza404() {
        Usuario sinEmpresa = Usuario.builder().id(20L).email("x@tipqr.com").empresa(null).build();
        when(usuarioRepository.findByEmail("x@tipqr.com")).thenReturn(Optional.of(sinEmpresa));

        assertThrows(ResourceNotFoundException.class, () -> empresaService.miEmpresa("x@tipqr.com"));
    }

    @Test
    void obtenerPorId_empresaPropia_funciona() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));

        EmpresaResponse resultado = empresaService.obtenerPorId(1L, EMAIL);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPorId_empresaAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));

        // El usuario pide la empresa 99 (de otro comercio) → no es la suya (id 1)
        assertThrows(ResourceNotFoundException.class, () -> empresaService.obtenerPorId(99L, EMAIL));
        verify(empresaRepository, never()).findById(anyLong());
    }

    @Test
    void actualizar_empresaAjena_lanza404YNoGuarda() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        EmpresaRequest request = new EmpresaRequest();
        request.setNombre("Hackeada");

        assertThrows(ResourceNotFoundException.class,
                () -> empresaService.actualizar(99L, request, EMAIL));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_empresaAjena_lanza404() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));

        assertThrows(ResourceNotFoundException.class,
                () -> empresaService.cambiarEstado(99L, false, EMAIL));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void actualizar_empresaPropia_guardaCambios() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(dueno));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));
        EmpresaRequest request = new EmpresaRequest();
        request.setNombre("Mi Bar Renovado");

        EmpresaResponse resultado = empresaService.actualizar(1L, request, EMAIL);

        assertEquals("Mi Bar Renovado", resultado.getNombre());
        verify(empresaRepository).save(empresaPropia);
    }
}
