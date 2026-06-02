package tipqr.back.exception;

/**
 * Se lanza cuando un recurso solicitado no existe. Mapeada a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " con id " + id + " no encontrada");
    }
}
