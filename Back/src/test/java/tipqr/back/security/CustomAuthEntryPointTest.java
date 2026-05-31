package tipqr.back.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CustomAuthEntryPointTest {

    private final CustomAuthEntryPoint entryPoint = new CustomAuthEntryPoint();

    @Test
    void commence_retorna401ConCuerpoJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = mock(AuthenticationException.class);

        entryPoint.commence(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertTrue(response.getContentAsString().contains("Token requerido"));
    }
}
