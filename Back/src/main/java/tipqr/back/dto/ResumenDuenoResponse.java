package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** Resumen de propinas de la empresa para el dashboard del dueño. */
@Getter
@Builder
@AllArgsConstructor
public class ResumenDuenoResponse {

    private BigDecimal totalRecaudado;
    private int cantidadPropinas;
    private BigDecimal ticketPromedio;
    private List<ResumenSucursal> porSucursal;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ResumenSucursal {
        private Long sucursalId;
        private String sucursalNombre;
        private BigDecimal total;
        private int cantidad;
    }
}
