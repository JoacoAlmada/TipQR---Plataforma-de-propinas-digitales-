package tipqr.back.entity;

import org.junit.jupiter.api.Test;
import tipqr.back.entity.enums.TipoReporte;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReporteAutomaticoTest {

    @Test
    void builder_creaReporteConCampos() {
        Empresa empresa = Empresa.builder().nombre("Empresa Test").build();
        LocalDate desde = LocalDate.of(2026, 5, 1);
        LocalDate hasta = LocalDate.of(2026, 5, 31);

        ReporteAutomatico r = ReporteAutomatico.builder()
                .empresa(empresa)
                .tipoReporte(TipoReporte.MENSUAL)
                .periodoDesde(desde)
                .periodoHasta(hasta)
                .resumenGenerado("Total propinas: $15.000")
                .build();

        assertEquals(TipoReporte.MENSUAL, r.getTipoReporte());
        assertEquals(desde, r.getPeriodoDesde());
        assertEquals(hasta, r.getPeriodoHasta());
        assertEquals("Total propinas: $15.000", r.getResumenGenerado());
        assertEquals(empresa, r.getEmpresa());
    }

    @Test
    void tiposReporte_sonValidos() {
        assertEquals(4, TipoReporte.values().length);
        assertNotNull(TipoReporte.valueOf("DIARIO"));
        assertNotNull(TipoReporte.valueOf("SEMANAL"));
        assertNotNull(TipoReporte.valueOf("MENSUAL"));
        assertNotNull(TipoReporte.valueOf("PERSONALIZADO"));
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        ReporteAutomatico r = new ReporteAutomatico();
        r.setTipoReporte(TipoReporte.DIARIO);
        r.setResumenGenerado("Resumen del día");

        assertEquals(TipoReporte.DIARIO, r.getTipoReporte());
        assertEquals("Resumen del día", r.getResumenGenerado());
    }
}
