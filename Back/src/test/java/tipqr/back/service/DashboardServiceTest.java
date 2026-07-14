package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.HistorialPropinasResponse;
import tipqr.back.dto.RankingEmpleadoResponse;
import tipqr.back.dto.ReportePeriodoResponse;
import tipqr.back.dto.ResumenDuenoResponse;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Mesa;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.DistribucionPropina;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.DistribucionPropinaRepository;
import tipqr.back.repository.OrdenPropinaRepository;
import tipqr.back.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private OrdenPropinaRepository ordenRepository;
    @Mock private DistribucionPropinaRepository distribucionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private DashboardService dashboardService;

    private Empresa empresa;
    private Sucursal sucursal;
    private Empleado empleado;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(1L).nombre("Mi Bar").build();
        sucursal = Sucursal.builder().id(5L).empresa(empresa).nombre("Centro").build();
        empleado = Empleado.builder().id(20L).sucursal(sucursal).nombreVisible("Juan").build();
    }

    private OrdenPropina pagada(BigDecimal monto, Sucursal s) {
        return OrdenPropina.builder().codigo("X").monto(monto).estado(EstadoOrden.PAGADA).sucursal(s).build();
    }

    // ── Historial empleado ─────────────────────────────────────

    @Test
    void historialEmpleado_devuelveTotalesYDetalle() {
        Usuario user = Usuario.builder().email("juan@tipqr.com").empleado(empleado).build();
        OrdenPropina o1 = pagada(new BigDecimal("1000"), sucursal);
        o1.setMesa(Mesa.builder().numero(3).build());
        OrdenPropina o2 = pagada(new BigDecimal("1500"), sucursal);
        when(usuarioRepository.findByEmail("juan@tipqr.com")).thenReturn(Optional.of(user));
        when(ordenRepository.findByEmpleadoIdAndEstadoOrderByFechaPagoDesc(20L, EstadoOrden.PAGADA))
                .thenReturn(List.of(o1, o2));

        HistorialPropinasResponse res = dashboardService.historialEmpleado("juan@tipqr.com");

        assertEquals(2, res.getCantidad());
        assertEquals(0, res.getTotal().compareTo(new BigDecimal("2500")));
        assertEquals("Mesa 3", res.getPropinas().get(0).getMesa());
    }

    @Test
    void historialEmpleado_sinPropinas_devuelveCeros() {
        Usuario user = Usuario.builder().email("juan@tipqr.com").empleado(empleado).build();
        when(usuarioRepository.findByEmail("juan@tipqr.com")).thenReturn(Optional.of(user));
        when(ordenRepository.findByEmpleadoIdAndEstadoOrderByFechaPagoDesc(20L, EstadoOrden.PAGADA))
                .thenReturn(List.of());

        HistorialPropinasResponse res = dashboardService.historialEmpleado("juan@tipqr.com");

        assertEquals(0, res.getCantidad());
        assertEquals(0, res.getTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void historialEmpleado_incluyeParteDeDistribucionesGrupales() {
        Usuario user = Usuario.builder().email("juan@tipqr.com").empleado(empleado).build();
        OrdenPropina individual = pagada(new BigDecimal("1000"), sucursal);
        OrdenPropina grupal = OrdenPropina.builder().codigo("G1").estado(EstadoOrden.PAGADA)
                .sucursal(sucursal).tipoPropina(TipoPropina.GRUPAL)
                .grupoPropina(GrupoPropina.builder().id(30L).nombre("Equipo").build()).build();
        DistribucionPropina dist = DistribucionPropina.builder()
                .ordenPropina(grupal).empleado(empleado).montoAsignado(new BigDecimal("500")).build();
        when(usuarioRepository.findByEmail("juan@tipqr.com")).thenReturn(Optional.of(user));
        when(ordenRepository.findByEmpleadoIdAndEstadoOrderByFechaPagoDesc(20L, EstadoOrden.PAGADA))
                .thenReturn(List.of(individual));
        when(distribucionRepository.findByEmpleadoIdOrderByOrdenPropina_FechaPagoDesc(20L))
                .thenReturn(List.of(dist));

        HistorialPropinasResponse res = dashboardService.historialEmpleado("juan@tipqr.com");

        assertEquals(2, res.getCantidad());
        assertEquals(0, res.getTotal().compareTo(new BigDecimal("1500"))); // 1000 individual + 500 grupal
        assertTrue(res.getPropinas().stream().anyMatch(p -> "GRUPAL".equals(p.getTipo())));
    }

    @Test
    void historialEmpleado_usuarioSinEmpleado_lanza404() {
        Usuario user = Usuario.builder().email("dueno@tipqr.com").build();
        when(usuarioRepository.findByEmail("dueno@tipqr.com")).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> dashboardService.historialEmpleado("dueno@tipqr.com"));
    }

    // ── Resumen dueño ──────────────────────────────────────────

    @Test
    void resumenDueno_calculaTotalesPromedioYPorSucursal() {
        Usuario dueno = Usuario.builder().email("dueno@tipqr.com").empresa(empresa).build();
        Sucursal norte = Sucursal.builder().id(6L).empresa(empresa).nombre("Norte").build();
        when(usuarioRepository.findByEmail("dueno@tipqr.com")).thenReturn(Optional.of(dueno));
        when(ordenRepository.findBySucursal_Empresa_IdAndEstado(1L, EstadoOrden.PAGADA))
                .thenReturn(List.of(
                        pagada(new BigDecimal("1000"), sucursal),
                        pagada(new BigDecimal("3000"), sucursal),
                        pagada(new BigDecimal("2000"), norte)));

        ResumenDuenoResponse res = dashboardService.resumenDueno("dueno@tipqr.com");

        assertEquals(0, res.getTotalRecaudado().compareTo(new BigDecimal("6000")));
        assertEquals(3, res.getCantidadPropinas());
        assertEquals(0, res.getTicketPromedio().compareTo(new BigDecimal("2000.00")));
        assertEquals(2, res.getPorSucursal().size());
        ResumenDuenoResponse.ResumenSucursal centro = res.getPorSucursal().stream()
                .filter(s -> s.getSucursalId().equals(5L)).findFirst().orElseThrow();
        assertEquals(2, centro.getCantidad());
        assertEquals(0, centro.getTotal().compareTo(new BigDecimal("4000")));
    }

    // ── Ranking ────────────────────────────────────────────────

    @Test
    void ranking_sumaIndividualYGrupalYOrdenaDesc() {
        Usuario dueno = Usuario.builder().email("dueno@tipqr.com").empresa(empresa).build();
        Empleado sofia = Empleado.builder().id(20L).nombreVisible("Sofia").sucursal(sucursal).build();
        Empleado tomas = Empleado.builder().id(21L).nombreVisible("Tomas").sucursal(sucursal).build();
        OrdenPropina oSofia = pagada(new BigDecimal("500"), sucursal); oSofia.setEmpleado(sofia);
        OrdenPropina oTomas = pagada(new BigDecimal("1000"), sucursal); oTomas.setEmpleado(tomas);
        OrdenPropina grupal = OrdenPropina.builder().codigo("G").estado(EstadoOrden.PAGADA).sucursal(sucursal)
                .tipoPropina(TipoPropina.GRUPAL).build();
        DistribucionPropina dSofia = DistribucionPropina.builder()
                .ordenPropina(grupal).empleado(sofia).montoAsignado(new BigDecimal("300")).build();

        when(usuarioRepository.findByEmail("dueno@tipqr.com")).thenReturn(Optional.of(dueno));
        when(ordenRepository.findBySucursal_Empresa_IdAndEstado(1L, EstadoOrden.PAGADA))
                .thenReturn(List.of(oSofia, oTomas));
        when(distribucionRepository.findByOrdenPropina_Sucursal_Empresa_Id(1L))
                .thenReturn(List.of(dSofia));

        List<RankingEmpleadoResponse> r = dashboardService.ranking("dueno@tipqr.com");

        assertEquals(2, r.size());
        // Sofia: 500 + 300 = 800; Tomas: 1000 -> Tomas primero
        assertEquals("Tomas", r.get(0).getNombre());
        assertEquals(0, r.get(0).getTotal().compareTo(new BigDecimal("1000")));
        assertEquals("Sofia", r.get(1).getNombre());
        assertEquals(0, r.get(1).getTotal().compareTo(new BigDecimal("800")));
        assertEquals(2, r.get(1).getCantidad()); // 1 individual + 1 grupal
    }

    // ── Reporte por período ────────────────────────────────────

    @Test
    void reportePeriodo_calculaTotalesDelRango() {
        Usuario dueno = Usuario.builder().email("dueno@tipqr.com").empresa(empresa).build();
        when(usuarioRepository.findByEmail("dueno@tipqr.com")).thenReturn(Optional.of(dueno));
        when(ordenRepository.findBySucursal_Empresa_IdAndEstadoAndFechaPagoBetween(
                eq(1L), eq(EstadoOrden.PAGADA), any(), any()))
                .thenReturn(List.of(pagada(new BigDecimal("1000"), sucursal),
                                    pagada(new BigDecimal("2000"), sucursal)));

        ReportePeriodoResponse res = dashboardService.reportePeriodo(
                "dueno@tipqr.com", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 6));

        assertEquals(0, res.getTotalRecaudado().compareTo(new BigDecimal("3000")));
        assertEquals(2, res.getCantidadPropinas());
        assertEquals(0, res.getTicketPromedio().compareTo(new BigDecimal("1500.00")));
    }

    @Test
    void resumenDueno_sinDatos_devuelveCeros() {
        Usuario dueno = Usuario.builder().email("dueno@tipqr.com").empresa(empresa).build();
        when(usuarioRepository.findByEmail("dueno@tipqr.com")).thenReturn(Optional.of(dueno));
        when(ordenRepository.findBySucursal_Empresa_IdAndEstado(1L, EstadoOrden.PAGADA)).thenReturn(List.of());

        ResumenDuenoResponse res = dashboardService.resumenDueno("dueno@tipqr.com");

        assertEquals(0, res.getTotalRecaudado().compareTo(BigDecimal.ZERO));
        assertEquals(0, res.getCantidadPropinas());
        assertEquals(0, res.getTicketPromedio().compareTo(BigDecimal.ZERO));
        assertTrue(res.getPorSucursal().isEmpty());
    }
}
