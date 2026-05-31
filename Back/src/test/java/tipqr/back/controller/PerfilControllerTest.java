package tipqr.back.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tipqr.back.config.SecurityConfig;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.Rol;
import tipqr.back.repository.UsuarioRepository;
import tipqr.back.security.CustomAccessDeniedHandler;
import tipqr.back.security.CustomAuthEntryPoint;
import tipqr.back.security.JwtAuthFilter;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PerfilController.class)
@Import(SecurityConfig.class)
class PerfilControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UsuarioRepository usuarioRepository;
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

        doAnswer(inv -> {
            ((HttpServletResponse) inv.getArgument(1)).setStatus(401);
            return null;
        }).when(customAuthEntryPoint).commence(any(), any(), any());

        doAnswer(inv -> {
            ((HttpServletResponse) inv.getArgument(1)).setStatus(403);
            return null;
        }).when(customAccessDeniedHandler).handle(any(), any(), any());

        when(usuarioRepository.findByEmail(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);
            Rol rol = email.contains("emp") ? Rol.EMPLEADO : Rol.DUENO;
            return Optional.of(Usuario.builder()
                    .email(email)
                    .nombre("Test")
                    .apellido("User")
                    .rol(rol)
                    .build());
        });
    }

    @Test
    @WithMockUser(username = "admin@tipqr.com", roles = "DUENO")
    void miPerfil_autenticado_retorna200() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@tipqr.com"))
                .andExpect(jsonPath("$.rol").value("DUENO"));
    }

    @Test
    void miPerfil_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@tipqr.com", roles = "DUENO")
    void perfilAdmin_rolDueno_retorna200() throws Exception {
        mockMvc.perform(get("/api/admin/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceso").value("Panel administrativo"));
    }

    @Test
    @WithMockUser(username = "admin@tipqr.com", roles = "ENCARGADO")
    void perfilAdmin_rolEncargado_retorna200() throws Exception {
        mockMvc.perform(get("/api/admin/perfil"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "emp@tipqr.com", roles = "EMPLEADO")
    void perfilAdmin_rolEmpleado_retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/perfil"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "emp@tipqr.com", roles = "EMPLEADO")
    void perfilEmpleado_rolEmpleado_retorna200() throws Exception {
        mockMvc.perform(get("/api/empleado/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceso").value("Panel empleado"));
    }

    @Test
    @WithMockUser(username = "admin@tipqr.com", roles = "DUENO")
    void perfilEmpleado_rolDueno_retorna403() throws Exception {
        mockMvc.perform(get("/api/empleado/perfil"))
                .andExpect(status().isForbidden());
    }
}
