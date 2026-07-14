package tipqr.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TurnoAbrirRequest {

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;

    @NotNull(message = "El grupo de propina es obligatorio")
    private Long grupoId;

    /** Etiqueta opcional del turno (ej: "Noche"). */
    private String nombre;
}
