package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.entity.DistribucionPropina;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.GrupoPropinaEmpleado;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.entity.enums.TipoEventoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.repository.DistribucionPropinaRepository;
import tipqr.back.repository.EventoOrdenRepository;
import tipqr.back.repository.GrupoPropinaEmpleadoRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reparte una propina GRUPAL pagada entre los miembros del grupo, de forma equitativa.
 * Los centavos que no dividen justo se le suman al primer empleado (así la suma cierra exacto).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistribucionService {

    private static final String CRITERIO = "EQUITATIVO";

    private final GrupoPropinaEmpleadoRepository miembroRepository;
    private final DistribucionPropinaRepository distribucionRepository;
    private final EventoOrdenRepository eventoRepository;
    private final NotificacionService notificacionService;

    /**
     * Genera el reparto de una orden. Solo actúa si la orden es GRUPAL, tiene grupo y aún no fue repartida.
     */
    @Transactional
    public void distribuir(OrdenPropina orden) {
        if (orden.getTipoPropina() != TipoPropina.GRUPAL) {
            return; // las individuales van directo al empleado, no se reparten
        }
        GrupoPropina grupo = orden.getGrupoPropina();
        if (grupo == null) {
            log.warn("Orden grupal {} sin grupo asignado; no se puede repartir", orden.getCodigo());
            return;
        }
        if (distribucionRepository.existsByOrdenPropinaId(orden.getId())) {
            return; // idempotente: ya repartida
        }

        List<Empleado> miembros = miembroRepository
                .findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(grupo.getId())
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .map(GrupoPropinaEmpleado::getEmpleado)
                .toList();

        if (miembros.isEmpty()) {
            log.warn("Grupo {} sin miembros activos; la propina {} queda sin repartir",
                    grupo.getId(), orden.getCodigo());
            return;
        }

        int n = miembros.size();
        long totalCent = orden.getMonto().movePointRight(2).longValueExact();
        long baseCent = totalCent / n;
        long resto = totalCent % n;              // centavos que sobran (van al/los primeros)
        double porcentaje = redondear2(100.0 / n);

        for (int i = 0; i < n; i++) {
            long cent = baseCent + (i < resto ? 1 : 0);
            BigDecimal parte = BigDecimal.valueOf(cent).movePointLeft(2);
            distribucionRepository.save(DistribucionPropina.builder()
                    .ordenPropina(orden)
                    .empleado(miembros.get(i))
                    .montoAsignado(parte)
                    .porcentaje(porcentaje)
                    .criterio(CRITERIO)
                    .build());
            // Aviso a cada integrante de su parte del reparto.
            notificacionService.notificarPropinaRecibida(miembros.get(i), parte,
                    "(parte de una propina grupal de " + grupo.getNombre() + ")");
        }

        eventoRepository.save(tipqr.back.entity.EventoOrden.builder()
                .ordenPropina(orden)
                .tipoEvento(TipoEventoOrden.DISTRIBUCION_GENERADA)
                .descripcion("Repartida entre " + n + " integrantes de " + grupo.getNombre())
                .build());

        log.info("Propina {} repartida entre {} integrantes de {}", orden.getCodigo(), n, grupo.getNombre());
    }

    private double redondear2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
