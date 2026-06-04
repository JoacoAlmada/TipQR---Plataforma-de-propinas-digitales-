package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistroEstadoResponse {
    private String registroToken;
    private String estadoCuenta;
    private boolean emailVerificado;
}
