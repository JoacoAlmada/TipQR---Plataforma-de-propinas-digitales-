package tipqr.back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import tipqr.back.entity.enums.CategoriaNotificacion;
import tipqr.back.entity.enums.PrioridadNotificacion;

@Getter
@Setter
public class CrearNotificacionRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    private CategoriaNotificacion categoria;
    private PrioridadNotificacion prioridad;

    /** Si viene, se envía solo a los empleados de esa sucursal; si es null, a toda la empresa. */
    private Long sucursalId;
}
