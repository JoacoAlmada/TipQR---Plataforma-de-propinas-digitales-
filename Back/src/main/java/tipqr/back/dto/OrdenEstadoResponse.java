package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.OrdenPropina;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Estado de una orden visto desde la pantalla pública (sin login).
 * No expone datos internos de la empresa más allá de lo necesario para mostrar el ticket.
 */
@Getter
@Builder
@AllArgsConstructor
public class OrdenEstadoResponse {

    private String codigo;
    private String estado;
    private String tipoPropina;
    private BigDecimal monto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaPago;
    private String sucursalNombre;

    public static OrdenEstadoResponse fromEntity(OrdenPropina o) {
        return OrdenEstadoResponse.builder()
                .codigo(o.getCodigo())
                .estado(o.getEstado() != null ? o.getEstado().name() : null)
                .tipoPropina(o.getTipoPropina() != null ? o.getTipoPropina().name() : null)
                .monto(o.getMonto())
                .fechaCreacion(o.getFechaCreacion())
                .fechaExpiracion(o.getFechaExpiracion())
                .fechaPago(o.getFechaPago())
                .sucursalNombre(o.getSucursal() != null ? o.getSucursal().getNombre() : null)
                .build();
    }
}
