package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** Fila del ranking de empleados por propinas recibidas (individuales + parte de las grupales). */
@Getter
@Builder
@AllArgsConstructor
public class RankingEmpleadoResponse {

    private Long empleadoId;
    private String nombre;
    private String sucursal;
    private int cantidad;
    private BigDecimal total;
}
