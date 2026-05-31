package tipqr.back.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.Rol;
import tipqr.back.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    UsuarioService usuarioService;

    @Test
    void loadUserByUsername_usuarioExistente_retornaUserDetails() {
        Usuario usuario = Usuario.builder()
                .email("admin@tipqr.com")
                .password("hashed")
                .rol(Rol.DUENO)
                .build();

        when(usuarioRepository.findByEmail("admin@tipqr.com")).thenReturn(Optional.of(usuario));

        UserDetails result = usuarioService.loadUserByUsername("admin@tipqr.com");

        assertEquals("admin@tipqr.com", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DUENO")));
    }

    @Test
    void loadUserByUsername_emailInexistente_lanzaUsernameNotFoundException() {
        when(usuarioRepository.findByEmail("noexiste@tipqr.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioService.loadUserByUsername("noexiste@tipqr.com"));
    }
}
