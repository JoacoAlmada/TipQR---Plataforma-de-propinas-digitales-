package tipqr.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AsignarEmpleadoRequest {

    @NotNull(message = "El empleado es obligatorio")
    private Long empleadoId;
}
