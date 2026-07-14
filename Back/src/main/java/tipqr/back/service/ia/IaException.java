package tipqr.back.service.ia;

/** Error al contactar o usar el proveedor de IA. */
public class IaException extends RuntimeException {

    public IaException(String message) {
        super(message);
    }

    public IaException(String message, Throwable cause) {
        super(message, cause);
    }
}
