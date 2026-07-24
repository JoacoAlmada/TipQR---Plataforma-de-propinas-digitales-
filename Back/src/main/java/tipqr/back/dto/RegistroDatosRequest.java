package tipqr.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Corrección de los datos personales del dueño al retomar un registro rechazado. */
@Getter @Setter
public class RegistroDatosRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60)
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30)
    private String telefono;

    @NotBlank(message = "El CUIT es obligatorio")
    private String cuit;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;
}
