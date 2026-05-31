package tipqr.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tipqr.back.config.SecurityConfig;
import tipqr.back.dto.LoginRequest;
import tipqr.back.dto.LoginResponse;
import tipqr.back.security.CustomAccessDeniedHandler;
import tipqr.back.security.CustomAuthEntryPoint;
import tipqr.back.security.JwtAuthFilter;
import tipqr.back.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean JwtAuthFilter jwtAuthFilter;
    @MockitoBean CustomAuthEntryPoint customAuthEntryPoint;
    @MockitoBean CustomAccessDeniedHandler customAccessDeniedHandler;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(
                    (HttpServletRequest) inv.getArgument(0),
                    (HttpServletResponse) inv.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void login_credencialesValidas_retorna200ConToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@tipqr.com");
        req.setPassword("tipqr2026");

        when(authService.login(any())).thenReturn(
                new LoginResponse("jwt-token", "admin@tipqr.com", "DUENO", "Admin", "TipQR"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.rol").value("DUENO"))
                .andExpect(jsonPath("$.nombre").value("Admin"));
    }

    @Test
    void login_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"notanemail\",\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_passwordVacio_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@tipqr.com\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_credencialesInvalidas_retorna401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@tipqr.com");
        req.setPassword("wrongpass");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }
}
