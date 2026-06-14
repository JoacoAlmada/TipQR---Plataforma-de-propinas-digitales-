package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.GrupoPropina;

@Getter
@Builder
@AllArgsConstructor
public class GrupoPropinaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String tipoGrupo;
    private Boolean estado;
    private Long sucursalId;
    private String sucursalNombre;

    public static GrupoPropinaResponse fromEntity(GrupoPropina g) {
        return GrupoPropinaResponse.builder()
                .id(g.getId())
                .nombre(g.getNombre())
                .descripcion(g.getDescripcion())
                .tipoGrupo(g.getTipoGrupo())
                .estado(g.getEstado())
                .sucursalId(g.getSucursal() != null ? g.getSucursal().getId() : null)
                .sucursalNombre(g.getSucursal() != null ? g.getSucursal().getNombre() : null)
                .build();
    }
}
