package tipqr.back.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tipqr.back.config.SecurityConfig;
import tipqr.back.security.CustomAccessDeniedHandler;
import tipqr.back.security.CustomAuthEntryPoint;
import tipqr.back.security.JwtAuthFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTest {

    @Autowired MockMvc mockMvc;

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
    void health_retorna200ConStatusUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.app").value("TipQR Backend"));
    }
}
