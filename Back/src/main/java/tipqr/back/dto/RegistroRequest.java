package tipqr.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Alta self-service de un comercio: crea la empresa y su usuario dueño en un paso.
 */
@Getter @Setter
public class RegistroRequest {

    // ── Datos de la empresa ─────────────────────────────
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombreEmpresa;

    @Size(max = 80, message = "El rubro no puede superar los 80 caracteres")
    private String rubro;

    @Pattern(
            regexp = "^$|\\d{2}-?\\d{8}-?\\d{1}$",
            message = "El CUIT debe tener el formato XX-XXXXXXXX-X (11 dígitos)")
    private String cuit;

    // ── Datos del dueño ─────────────────────────────────
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60, message = "El apellido no puede superar los 60 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
