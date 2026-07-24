package tipqr.back.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaMultiEmpresaTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private static final String EMAIL = "dueno@demo.com";

    private EmpresaService service() {
        return new EmpresaService(empresaRepository, usuarioRepository);
    }

    private Usuario dueno(long id) {
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    @Test
    void crear_dejaLaNuevaComoActivaYConPropietario() {
        Usuario u = dueno(1L);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(empresaRepository.existsByCuit(anyString())).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class)))
                .thenAnswer(inv -> { Empresa e = inv.getArgument(0); e.setId(99L); return e; });

        EmpresaRequest req = new EmpresaRequest();
        req.setNombre("Bar Nuevo");
        req.setCuit("30-11111111-1");

        EmpresaResponse r = service().crear(req, EMAIL);

        assertTrue(r.isActiva());
        assertEquals("Bar Nuevo", r.getNombre());

        ArgumentCaptor<Empresa> cap = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(cap.capture());
        assertEquals(u, cap.getValue().getPropietario());
        assertEquals(99L, u.getEmpresa().getId());  // quedó como activa
        verify(usuarioRepository).save(u);
    }

    @Test
    void cambiarActiva_empresaPropia_cambiaLaActiva() {
        Usuario u = dueno(1L);
        Empresa propia = Empresa.builder().id(60L).nombre("Otra").propietario(u).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(empresaRepository.findById(60L)).thenReturn(Optional.of(propia));

        EmpresaResponse r = service().cambiarActiva(60L, EMAIL);

        assertTrue(r.isActiva());
        assertEquals(60L, u.getEmpresa().getId());
        verify(usuarioRepository).save(u);
    }

    @Test
    void cambiarActiva_empresaAjena_lanza404() {
        Usuario u = dueno(1L);
        Empresa ajena = Empresa.builder().id(50L).propietario(dueno(2L)).build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(empresaRepository.findById(50L)).thenReturn(Optional.of(ajena));

        assertThrows(ResourceNotFoundException.class, () -> service().cambiarActiva(50L, EMAIL));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void misEmpresas_backfilleaPropietarioYMarcaLaActiva() {
        Usuario u = dueno(1L);
        Empresa activa = Empresa.builder().id(10L).nombre("Activa").build(); // sin propietario
        u.setEmpresa(activa);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(empresaRepository.findByPropietarioIdOrderByNombreAsc(1L)).thenReturn(List.of());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        List<EmpresaResponse> res = service().misEmpresas(EMAIL);

        assertEquals(u, activa.getPropietario());  // backfill
        assertEquals(1, res.size());
        assertTrue(res.get(0).isActiva());
    }
}
