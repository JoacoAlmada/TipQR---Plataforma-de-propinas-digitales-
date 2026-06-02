package tipqr.back.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import tipqr.back.dto.ErrorResponse;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadCredentials_retorna401ConMensaje() {
        BadCredentialsException ex = new BadCredentialsException("Credenciales inválidas");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Credenciales inválidas", response.getBody().getError());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void handleNotFound_retorna404ConMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Empresa", 99L);

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getError().contains("no encontrada"));
    }

    @Test
    void handleDuplicate_retorna409ConMensaje() {
        DuplicateResourceException ex = new DuplicateResourceException("CUIT duplicado");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("CUIT duplicado", response.getBody().getError());
    }
}
