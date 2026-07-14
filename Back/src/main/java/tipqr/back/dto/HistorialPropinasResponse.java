package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** Historial de propinas del empleado: totales + detalle. */
@Getter
@Builder
@AllArgsConstructor
public class HistorialPropinasResponse {

    private int cantidad;
    private BigDecimal total;
    private List<PropinaEmpleadoResponse> propinas;
}
