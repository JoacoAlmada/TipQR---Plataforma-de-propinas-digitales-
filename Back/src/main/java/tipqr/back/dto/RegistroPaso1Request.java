package tipqr.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Paso 1 del registro: datos del usuario (dueño) + token de captcha.
 */
@Getter @Setter
public class RegistroPaso1Request {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30)
    private String telefono;

    @NotBlank(message = "El CUIT es obligatorio")
    private String cuit;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "Confirmá que no sos un robot")
    private String captchaToken;
}
