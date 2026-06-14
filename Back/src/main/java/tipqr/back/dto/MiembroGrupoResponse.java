package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.GrupoPropinaEmpleado;

@Getter
@Builder
@AllArgsConstructor
public class MiembroGrupoResponse {

    private Long empleadoId;
    private String nombreVisible;
    private String apellido;
    private String email;
    private String puesto;

    public static MiembroGrupoResponse fromEntity(GrupoPropinaEmpleado gpe) {
        Empleado e = gpe.getEmpleado();
        return MiembroGrupoResponse.builder()
                .empleadoId(e.getId())
                .nombreVisible(e.getNombreVisible())
                .apellido(e.getUsuario() != null ? e.getUsuario().getApellido() : null)
                .email(e.getUsuario() != null ? e.getUsuario().getEmail() : null)
                .puesto(e.getPuesto())
                .build();
    }
}
