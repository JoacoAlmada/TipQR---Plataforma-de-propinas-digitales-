package tipqr.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GrupoPropinaRequest {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @Size(max = 160, message = "La descripción no puede superar los 160 caracteres")
    private String descripcion;

    @Size(max = 60, message = "El tipo no puede superar los 60 caracteres")
    private String tipoGrupo;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;
}
