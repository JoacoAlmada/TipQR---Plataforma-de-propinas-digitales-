package tipqr.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmpresaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @Size(max = 80, message = "El rubro no puede superar los 80 caracteres")
    private String rubro;

    @Pattern(
            regexp = "^$|\\d{2}-?\\d{8}-?\\d{1}$",
            message = "El CUIT debe tener el formato XX-XXXXXXXX-X (11 dígitos)")
    private String cuit;

    @Email(message = "El email de contacto no es válido")
    private String emailContacto;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;
}
