package tipqr.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmpleadoRequest {

    @NotBlank(message = "El nombre visible es obligatorio")
    @Size(max = 80, message = "El nombre visible no puede superar los 80 caracteres")
    private String nombreVisible;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 80)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @Size(max = 60, message = "El puesto no puede superar los 60 caracteres")
    private String puesto;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;
}
