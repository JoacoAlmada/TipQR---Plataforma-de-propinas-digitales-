package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.CodigoQR;

@Getter
@Builder
@AllArgsConstructor
public class QrResponse {

    private Long id;
    private String codigo;
    private String tipoDestino;
    private String url;
    private Boolean activo;
    private Long destinoId;
    private String destinoNombre;
    private Long sucursalId;
    private String sucursalNombre;
    /** Ruta del endpoint para obtener la imagen PNG del QR. */
    private String imagenUrl;

    public static QrResponse fromEntity(CodigoQR q) {
        Long destinoId = null;
        String destinoNombre = null;
        if (q.getMesa() != null) {
            destinoId = q.getMesa().getId();
            destinoNombre = "Mesa " + q.getMesa().getNumero();
        } else if (q.getEmpleado() != null) {
            destinoId = q.getEmpleado().getId();
            destinoNombre = q.getEmpleado().getNombreVisible();
        } else if (q.getGrupoPropina() != null) {
            destinoId = q.getGrupoPropina().getId();
            destinoNombre = q.getGrupoPropina().getNombre();
        }

        return QrResponse.builder()
                .id(q.getId())
                .codigo(q.getCodigo())
                .tipoDestino(q.getTipoDestino() != null ? q.getTipoDestino().name() : null)
                .url(q.getUrl())
                .activo(q.getActivo())
                .destinoId(destinoId)
                .destinoNombre(destinoNombre)
                .sucursalId(q.getSucursal() != null ? q.getSucursal().getId() : null)
                .sucursalNombre(q.getSucursal() != null ? q.getSucursal().getNombre() : null)
                .imagenUrl("/api/qr/" + q.getId() + "/imagen")
                .build();
    }
}
