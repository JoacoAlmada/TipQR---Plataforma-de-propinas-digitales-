package tipqr.back.service.ia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.RankingEmpleadoResponse;
import tipqr.back.dto.ReporteAutomaticoResponse;
import tipqr.back.dto.ResumenDuenoResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.ReporteAutomatico;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.ReporteAutomaticoRepository;
import tipqr.back.repository.UsuarioRepository;
import tipqr.back.service.DashboardService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgenteReporteServiceTest {

    @Mock private ProveedorIa proveedorIa;
    @Mock private DashboardService dashboardService;
    @Mock private ReporteAutomaticoRepository reporteRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private static final String EMAIL = "dueno@demo.com";

    private AgenteReporteService servicio() {
        return new AgenteReporteService(proveedorIa, dashboardService, reporteRepository, usuarioRepository);
    }

    private void stubComunes() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        Usuario u = new Usuario();
        u.setEmpresa(empresa);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));

        when(dashboardService.resumenDueno(EMAIL)).thenReturn(ResumenDuenoResponse.builder()
                .totalRecaudado(new BigDecimal("1000")).cantidadPropinas(4)
                .ticketPromedio(new BigDecimal("250")).porSucursal(List.of()).build());
        when(dashboardService.ranking(EMAIL)).thenReturn(List.of(
                RankingEmpleadoResponse.builder().empleadoId(1L).nombre("Sofía").sucursal("Centro")
                        .cantidad(3).total(new BigDecimal("700")).build()));
        when(reporteRepository.save(any(ReporteAutomatico.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void generar_conIa_persisteYMarcaGeneradoPorIa() {
        stubComunes();
        when(proveedorIa.disponible()).thenReturn(true);
        when(proveedorIa.completar(anyString(), anyString(), eq(false)))
                .thenReturn("Resumen ejecutivo redactado por la IA.");

        ReporteAutomaticoResponse r = servicio().generar(EMAIL);

        assertTrue(r.isGeneradoPorIa());
        assertEquals("Resumen ejecutivo redactado por la IA.", r.getContenido());
        assertEquals(new BigDecimal("1000"), r.getTotalRecaudado());
        verify(reporteRepository).save(any(ReporteAutomatico.class));
    }

    @Test
    void generar_sinProveedor_usaResumenLocal() {
        stubComunes();
        when(proveedorIa.disponible()).thenReturn(false);

        ReporteAutomaticoResponse r = servicio().generar(EMAIL);

        assertFalse(r.isGeneradoPorIa());
        assertTrue(r.getContenido().contains("Panorama general"));
        verify(proveedorIa, never()).completar(anyString(), anyString(), anyBoolean());
    }

    @Test
    void generar_siLaIaFalla_caeAlResumenLocal() {
        stubComunes();
        when(proveedorIa.disponible()).thenReturn(true);
        when(proveedorIa.completar(anyString(), anyString(), eq(false)))
                .thenThrow(new IaException("sin conexión"));

        ReporteAutomaticoResponse r = servicio().generar(EMAIL);

        assertFalse(r.isGeneradoPorIa());
        assertTrue(r.getContenido().contains("Panorama general"));
    }

    @Test
    void eliminar_borraElReporteDeLaEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        Usuario u = new Usuario();
        u.setEmpresa(empresa);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        ReporteAutomatico r = ReporteAutomatico.builder().id(5L).build();
        when(reporteRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(r));

        servicio().eliminar(5L, EMAIL);

        verify(reporteRepository).delete(r);
    }

    @Test
    void eliminar_reporteAjeno_lanza404() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        Usuario u = new Usuario();
        u.setEmpresa(empresa);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(reporteRepository.findByIdAndEmpresaId(9L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> servicio().eliminar(9L, EMAIL));
    }

    @Test
    void listar_devuelveElHistorialDeLaEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setId(7L);
        Usuario u = new Usuario();
        u.setEmpresa(empresa);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(reporteRepository.findByEmpresaIdOrderByFechaGeneracionDesc(7L)).thenReturn(List.of(
                ReporteAutomatico.builder().id(1L).titulo("Resumen").resumenGenerado("texto")
                        .generadoPorIa(true).build()));

        List<ReporteAutomaticoResponse> lista = servicio().listar(EMAIL);

        assertEquals(1, lista.size());
        assertEquals("texto", lista.get(0).getContenido());
    }
}
