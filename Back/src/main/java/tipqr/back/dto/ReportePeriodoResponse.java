package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Reporte de propinas pagadas de la empresa en un rango de fechas. */
@Getter
@Builder
@AllArgsConstructor
public class ReportePeriodoResponse {

    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal totalRecaudado;
    private int cantidadPropinas;
    private BigDecimal ticketPromedio;
}
