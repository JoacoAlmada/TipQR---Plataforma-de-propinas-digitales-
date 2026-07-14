package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Turno;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TurnoResponse {

    private Long id;
    private Long sucursalId;
    private String sucursalNombre;
    private Long grupoId;
    private String grupoNombre;
    private String nombre;
    private Boolean activo;
    private String abiertoPor;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    public static TurnoResponse fromEntity(Turno t) {
        String abiertoPor = null;
        if (t.getAbiertoPor() != null) {
            abiertoPor = (t.getAbiertoPor().getNombre() + " " +
                    (t.getAbiertoPor().getApellido() != null ? t.getAbiertoPor().getApellido() : "")).trim();
        }
        return TurnoResponse.builder()
                .id(t.getId())
                .sucursalId(t.getSucursal() != null ? t.getSucursal().getId() : null)
                .sucursalNombre(t.getSucursal() != null ? t.getSucursal().getNombre() : null)
                .grupoId(t.getGrupoPropina() != null ? t.getGrupoPropina().getId() : null)
                .grupoNombre(t.getGrupoPropina() != null ? t.getGrupoPropina().getNombre() : null)
                .nombre(t.getNombre())
                .activo(t.getActivo())
                .abiertoPor(abiertoPor)
                .fechaApertura(t.getFechaApertura())
                .fechaCierre(t.getFechaCierre())
                .build();
    }
}
