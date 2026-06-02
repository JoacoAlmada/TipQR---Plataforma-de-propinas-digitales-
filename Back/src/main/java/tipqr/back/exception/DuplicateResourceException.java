package tipqr.back.exception;

/**
 * Se lanza cuando se intenta crear/actualizar un recurso con un valor único
 * que ya existe (ej: CUIT duplicado). Mapeada a HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
