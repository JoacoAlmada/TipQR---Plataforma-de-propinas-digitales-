package tipqr.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.entity.*;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.TipoEventoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.repository.DistribucionPropinaRepository;
import tipqr.back.repository.EventoOrdenRepository;
import tipqr.back.repository.GrupoPropinaEmpleadoRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistribucionServiceTest {

    @Mock private GrupoPropinaEmpleadoRepository miembroRepository;
    @Mock private DistribucionPropinaRepository distribucionRepository;
    @Mock private EventoOrdenRepository eventoRepository;
    @Mock private NotificacionService notificacionService;
    @InjectMocks private DistribucionService distribucionService;

    private Sucursal sucursal;
    private GrupoPropina grupo;

    @BeforeEach
    void setUp() {
        sucursal = Sucursal.builder().id(5L).nombre("Centro").build();
        grupo = GrupoPropina.builder().id(30L).sucursal(sucursal).nombre("Equipo Noche").build();
    }

    private GrupoPropinaEmpleado miembro(long id, String nombre) {
        Empleado e = Empleado.builder().id(id).nombreVisible(nombre).sucursal(sucursal).build();
        return GrupoPropinaEmpleado.builder().empleado(e).activo(true).build();
    }

    private OrdenPropina ordenGrupal(String monto) {
        return OrdenPropina.builder().id(100L).codigo("ORD1").sucursal(sucursal)
                .tipoPropina(TipoPropina.GRUPAL).grupoPropina(grupo).monto(new BigDecimal(monto))
                .estado(EstadoOrden.PAGADA).build();
    }

    @Test
    void distribuir_montoDivisible_repartoEquitativo() {
        when(distribucionRepository.existsByOrdenPropinaId(100L)).thenReturn(false);
        when(miembroRepository.findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(30L))
                .thenReturn(List.of(miembro(1, "Ana"), miembro(2, "Beto")));

        distribucionService.distribuir(ordenGrupal("1000.00"));

        ArgumentCaptor<DistribucionPropina> cap = ArgumentCaptor.forClass(DistribucionPropina.class);
        verify(distribucionRepository, times(2)).save(cap.capture());
        BigDecimal total = cap.getAllValues().stream().map(DistribucionPropina::getMontoAsignado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, total.compareTo(new BigDecimal("1000.00")));
        cap.getAllValues().forEach(d -> assertEquals(0, d.getMontoAsignado().compareTo(new BigDecimal("500.00"))));
        verify(eventoRepository).save(argThat(e -> e.getTipoEvento() == TipoEventoOrden.DISTRIBUCION_GENERADA));
    }

    @Test
    void distribuir_conCentavosSobrantes_vanAlPrimeroYSumaExacta() {
        when(distribucionRepository.existsByOrdenPropinaId(100L)).thenReturn(false);
        when(miembroRepository.findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(30L))
                .thenReturn(List.of(miembro(1, "Ana"), miembro(2, "Beto"), miembro(3, "Caro")));

        distribucionService.distribuir(ordenGrupal("1000.00")); // 1000 / 3

        ArgumentCaptor<DistribucionPropina> cap = ArgumentCaptor.forClass(DistribucionPropina.class);
        verify(distribucionRepository, times(3)).save(cap.capture());
        List<DistribucionPropina> ds = cap.getAllValues();
        // La suma cierra exacta
        BigDecimal total = ds.stream().map(DistribucionPropina::getMontoAsignado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, total.compareTo(new BigDecimal("1000.00")));
        // El primero recibe el centavo sobrante
        assertEquals(0, ds.get(0).getMontoAsignado().compareTo(new BigDecimal("333.34")));
        assertEquals(0, ds.get(1).getMontoAsignado().compareTo(new BigDecimal("333.33")));
        assertEquals(0, ds.get(2).getMontoAsignado().compareTo(new BigDecimal("333.33")));
    }

    @Test
    void distribuir_ordenIndividual_noHaceNada() {
        OrdenPropina individual = OrdenPropina.builder().id(101L).codigo("IND")
                .tipoPropina(TipoPropina.INDIVIDUAL).monto(new BigDecimal("500")).build();

        distribucionService.distribuir(individual);

        verify(distribucionRepository, never()).save(any());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void distribuir_yaRepartida_noVuelveARepartir() {
        when(distribucionRepository.existsByOrdenPropinaId(100L)).thenReturn(true);

        distribucionService.distribuir(ordenGrupal("1000.00"));

        verify(distribucionRepository, never()).save(any());
    }

    @Test
    void distribuir_grupoSinMiembros_noReparte() {
        when(distribucionRepository.existsByOrdenPropinaId(100L)).thenReturn(false);
        when(miembroRepository.findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(30L)).thenReturn(List.of());

        distribucionService.distribuir(ordenGrupal("1000.00"));

        verify(distribucionRepository, never()).save(any());
        verify(eventoRepository, never()).save(any());
    }
}
