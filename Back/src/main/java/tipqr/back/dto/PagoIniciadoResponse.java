package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Respuesta al iniciar el pago: la URL del Checkout Pro a la que se redirige al cliente.
 */
@Getter
@Builder
@AllArgsConstructor
public class PagoIniciadoResponse {

    private String ordenCodigo;
    private String preferenceId;
    private String checkoutUrl;
    private String publicKey;
}
