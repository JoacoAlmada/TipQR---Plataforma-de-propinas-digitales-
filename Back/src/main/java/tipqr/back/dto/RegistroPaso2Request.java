package tipqr.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Paso 2 del registro: datos del comercio.
 */
@Getter @Setter
public class RegistroPaso2Request {

    @NotBlank(message = "El nombre del comercio es obligatorio")
    @Size(max = 120)
    private String nombreEmpresa;

    @Size(max = 120)
    private String nombreFantasia;

    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotBlank(message = "La numeración es obligatoria")
    private String numeracion;

    @Pattern(
            regexp = "^$|\\d{2}-?\\d{8}-?\\d{1}$",
            message = "El CUIT debe tener el formato XX-XXXXXXXX-X (11 dígitos)")
    private String cuit;

    @NotBlank(message = "El rubro es obligatorio")
    private String rubro;
}
