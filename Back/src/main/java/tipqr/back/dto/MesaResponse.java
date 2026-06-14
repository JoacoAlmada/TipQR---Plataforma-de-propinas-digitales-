package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Mesa;

@Getter
@Builder
@AllArgsConstructor
public class MesaResponse {

    private Long id;
    private Integer numero;
    private String descripcion;
    private Boolean estado;
    private Long sucursalId;
    private String sucursalNombre;

    public static MesaResponse fromEntity(Mesa m) {
        return MesaResponse.builder()
                .id(m.getId())
                .numero(m.getNumero())
                .descripcion(m.getDescripcion())
                .estado(m.getEstado())
                .sucursalId(m.getSucursal() != null ? m.getSucursal().getId() : null)
                .sucursalNombre(m.getSucursal() != null ? m.getSucursal().getNombre() : null)
                .build();
    }
}
