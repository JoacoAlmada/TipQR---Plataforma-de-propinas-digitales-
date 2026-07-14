package tipqr.back.dto;

import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.ReporteAutomatico;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/** Reporte automático para el frontend. */
@Getter
@Builder
public class ReporteAutomaticoResponse {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private String titulo;
    private String contenido;
    private BigDecimal totalRecaudado;
    private Integer cantidadPropinas;
    private BigDecimal ticketPromedio;
    private boolean generadoPorIa;
    private String fecha;

    public static ReporteAutomaticoResponse fromEntity(ReporteAutomatico r) {
        return ReporteAutomaticoResponse.builder()
                .id(r.getId())
                .titulo(r.getTitulo())
                .contenido(r.getResumenGenerado())
                .totalRecaudado(r.getTotalRecaudado())
                .cantidadPropinas(r.getCantidadPropinas())
                .ticketPromedio(r.getTicketPromedio())
                .generadoPorIa(r.isGeneradoPorIa())
                .fecha(r.getFechaGeneracion() != null ? r.getFechaGeneracion().format(FMT) : null)
                .build();
    }
}
