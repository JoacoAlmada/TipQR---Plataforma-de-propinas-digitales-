package tipqr.back.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import tipqr.back.dto.LoginRequest;
import tipqr.back.dto.LoginResponse;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.Rol;
import tipqr.back.repository.UsuarioRepository;
import tipqr.back.security.JwtUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock UsuarioService usuarioService;
    @Mock JwtUtil jwtUtil;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    private LoginRequest buildRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    @Test
    void login_credencialesValidas_retornaLoginResponse() {
        Usuario usuario = Usuario.builder()
                .email("admin@tipqr.com")
                .password("hashed")
                .nombre("Admin")
                .apellido("TipQR")
                .rol(Rol.DUENO)
                .estado(true)
                .build();

        UserDetails userDetails = User.builder()
                .username("admin@tipqr.com").password("hashed").roles("DUENO").build();

        when(usuarioRepository.findByEmail("admin@tipqr.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("tipqr2026", "hashed")).thenReturn(true);
        when(usuarioService.loadUserByUsername("admin@tipqr.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = authService.login(buildRequest("admin@tipqr.com", "tipqr2026"));

        assertEquals("jwt-token", response.getToken());
        assertEquals("admin@tipqr.com", response.getEmail());
        assertEquals("DUENO", response.getRol());
        assertEquals("Admin", response.getNombre());
        assertEquals("TipQR", response.getApellido());
    }

    @Test
    void login_emailInexistente_lanzaBadCredentialsException() {
        when(usuarioRepository.findByEmail("noexiste@tipqr.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(buildRequest("noexiste@tipqr.com", "pass")));
    }

    @Test
    void login_usuarioInactivo_lanzaBadCredentialsException() {
        Usuario usuario = Usuario.builder()
                .email("inactivo@tipqr.com")
                .password("hashed")
                .estado(false)
                .build();

        when(usuarioRepository.findByEmail("inactivo@tipqr.com")).thenReturn(Optional.of(usuario));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(buildRequest("inactivo@tipqr.com", "pass")));
    }

    @Test
    void login_passwordIncorrecto_lanzaBadCredentialsException() {
        Usuario usuario = Usuario.builder()
                .email("admin@tipqr.com")
                .password("hashed")
                .estado(true)
                .build();

        when(usuarioRepository.findByEmail("admin@tipqr.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(buildRequest("admin@tipqr.com", "wrongpass")));

        verify(jwtUtil, never()).generateToken(any());
    }
}
