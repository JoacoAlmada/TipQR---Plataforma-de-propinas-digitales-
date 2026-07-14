package tipqr.back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Pedido para que el agente redacte un aviso a partir de una instrucción informal. */
@Getter
@Setter
public class RedactarNotificacionRequest {

    @NotBlank(message = "Escribí qué querés avisar")
    private String instruccion;
}
