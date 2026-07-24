package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Sucursal;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SucursalResponse {

    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private String telefono;
    private Boolean estado;
    private LocalDateTime fechaCreacion;

    public static SucursalResponse fromEntity(Sucursal s) {
        return SucursalResponse.builder()
                .id(s.getId())
                .nombre(s.getNombre())
                .direccion(s.getDireccion())
                .latitud(s.getLatitud())
                .longitud(s.getLongitud())
                .telefono(s.getTelefono())
                .estado(s.getEstado())
                .fechaCreacion(s.getFechaCreacion())
                .build();
    }
}
