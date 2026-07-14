package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.DistribucionPropina;
import tipqr.back.entity.OrdenPropina;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una propina recibida por el empleado (item del historial): individual o parte de una grupal. */
@Getter
@Builder
@AllArgsConstructor
public class PropinaEmpleadoResponse {

    private String codigo;
    private BigDecimal monto;
    private String mesa;
    private LocalDateTime fechaPago;
    /** INDIVIDUAL o GRUPAL, para distinguir en la UI. */
    private String tipo;

    public static PropinaEmpleadoResponse fromEntity(OrdenPropina o) {
        return PropinaEmpleadoResponse.builder()
                .codigo(o.getCodigo())
                .monto(o.getMonto())
                .mesa(o.getMesa() != null ? "Mesa " + o.getMesa().getNumero() : null)
                .fechaPago(o.getFechaPago())
                .tipo("INDIVIDUAL")
                .build();
    }

    public static PropinaEmpleadoResponse fromDistribucion(DistribucionPropina d) {
        OrdenPropina o = d.getOrdenPropina();
        return PropinaEmpleadoResponse.builder()
                .codigo(o.getCodigo())
                .monto(d.getMontoAsignado())
                .mesa(o.getMesa() != null ? "Mesa " + o.getMesa().getNumero() : null)
                .fechaPago(o.getFechaPago())
                .tipo("GRUPAL")
                .build();
    }
}
