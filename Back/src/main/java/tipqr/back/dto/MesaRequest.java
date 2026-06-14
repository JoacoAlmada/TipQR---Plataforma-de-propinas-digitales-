package tipqr.back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MesaRequest {

    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número debe ser mayor a 0")
    private Integer numero;

    @Size(max = 120, message = "La descripción no puede superar los 120 caracteres")
    private String descripcion;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;
}
