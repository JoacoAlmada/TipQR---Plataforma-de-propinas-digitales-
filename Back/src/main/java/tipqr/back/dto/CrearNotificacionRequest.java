package tipqr.back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import tipqr.back.entity.enums.CategoriaNotificacion;
import tipqr.back.entity.enums.PrioridadNotificacion;
import tipqr.back.entity.enums.Rol;

@Getter
@Setter
public class CrearNotificacionRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    private CategoriaNotificacion categoria;
    private PrioridadNotificacion prioridad;

    // ── Segmentación del destinatario (se aplica la primera que venga informada) ──
    /** Empleados del grupo del turno indicado. */
    private Long turnoId;
    /** Empleados de la empresa con este rol (ENCARGADO / EMPLEADO). */
    private Rol rol;
    /** Empleados de esa sucursal. */
    private Long sucursalId;
    // Si no viene ninguno de los anteriores, se envía a toda la empresa.

    /** True si el aviso lo redactó el agente de IA (y el usuario lo confirmó): marca origen AGENTE. */
    private boolean asistidoPorIa;
}
