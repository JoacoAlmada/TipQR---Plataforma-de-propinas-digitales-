package tipqr.back.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CrearOrdenPublicaRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1.0", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    /**
     * Solo para QR de mesa: si viene un empleadoId, la propina es individual para ese mozo
     * (debe ser del turno activo); si viene null, se reparte entre el equipo del turno activo.
     */
    private Long empleadoId;
}
