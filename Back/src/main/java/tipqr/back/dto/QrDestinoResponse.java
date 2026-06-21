package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.CodigoQR;

/**
 * Información del destino de un QR para la pantalla pública (sin login).
 */
@Getter
@Builder
@AllArgsConstructor
public class QrDestinoResponse {

    private String codigo;
    private String tipoDestino;
    private String destinoNombre;
    private String sucursalNombre;
    private String empresaNombre;
    private Boolean activo;

    public static QrDestinoResponse fromEntity(CodigoQR q) {
        String destinoNombre = null;
        if (q.getMesa() != null) {
            destinoNombre = "Mesa " + q.getMesa().getNumero();
        } else if (q.getEmpleado() != null) {
            destinoNombre = q.getEmpleado().getNombreVisible();
        } else if (q.getGrupoPropina() != null) {
            destinoNombre = q.getGrupoPropina().getNombre();
        }

        String empresaNombre = null;
        if (q.getSucursal() != null && q.getSucursal().getEmpresa() != null) {
            empresaNombre = q.getSucursal().getEmpresa().getNombre();
        }

        return QrDestinoResponse.builder()
                .codigo(q.getCodigo())
                .tipoDestino(q.getTipoDestino() != null ? q.getTipoDestino().name() : null)
                .destinoNombre(destinoNombre)
                .sucursalNombre(q.getSucursal() != null ? q.getSucursal().getNombre() : null)
                .empresaNombre(empresaNombre)
                .activo(q.getActivo())
                .build();
    }
}
