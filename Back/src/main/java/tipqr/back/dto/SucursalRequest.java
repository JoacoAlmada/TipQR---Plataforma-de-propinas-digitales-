package tipqr.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SucursalRequest {

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @Size(max = 160, message = "La dirección no puede superar los 160 caracteres")
    private String direccion;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;
}
