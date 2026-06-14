package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.enums.Rol;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@AllArgsConstructor
public class EmpleadoResponse {

    private Long id;
    private String nombreVisible;
    private String apellido;
    private String email;
    private String puesto;
    private Boolean estado;
    private Boolean esEncargado;
    private Long sucursalId;
    private String sucursalNombre;
    private LocalDateTime fechaAlta;

    /** Solo se completa al crear: contraseña temporal generada para el empleado. */
    private String passwordTemporal;

    public static EmpleadoResponse fromEntity(Empleado e) {
        boolean encargado = e.getUsuario() != null && e.getUsuario().getRol() == Rol.ENCARGADO;
        return EmpleadoResponse.builder()
                .id(e.getId())
                .nombreVisible(e.getNombreVisible())
                .apellido(e.getUsuario() != null ? e.getUsuario().getApellido() : null)
                .email(e.getUsuario() != null ? e.getUsuario().getEmail() : null)
                .puesto(e.getPuesto())
                .estado(e.getEstado())
                .esEncargado(encargado)
                .sucursalId(e.getSucursal() != null ? e.getSucursal().getId() : null)
                .sucursalNombre(e.getSucursal() != null ? e.getSucursal().getNombre() : null)
                .fechaAlta(e.getFechaAlta())
                .build();
    }
}
