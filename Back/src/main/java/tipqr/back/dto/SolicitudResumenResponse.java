package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Usuario;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SolicitudResumenResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String empresaNombre;
    private LocalDateTime fechaSolicitud;

    public static SolicitudResumenResponse fromEntity(Usuario u) {
        return SolicitudResumenResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .empresaNombre(u.getEmpresa() != null ? u.getEmpresa().getNombre() : null)
                .fechaSolicitud(u.getFechaCreacion())
                .build();
    }
}
