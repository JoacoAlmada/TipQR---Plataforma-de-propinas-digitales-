package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Notificacion;
import tipqr.back.entity.NotificacionDestinatario;

import java.time.LocalDateTime;
import java.util.List;

/** Notificación tal como la ve quien la envió: a quién llegó y cuántos la leyeron. */
@Getter
@Builder
@AllArgsConstructor
public class NotificacionEnviadaResponse {

    private Long id;            // id de la Notificacion
    private String titulo;
    private String mensaje;
    private String categoria;
    private String prioridad;
    private String origen;
    private String destino;     // sucursal o "Toda la empresa"
    private int destinatarios;  // a cuántos se envió
    private int leidas;         // cuántos la leyeron
    private LocalDateTime fecha;

    public static NotificacionEnviadaResponse fromEntity(Notificacion n) {
        List<NotificacionDestinatario> dests = n.getDestinatarios();
        int total = dests != null ? dests.size() : 0;
        int leidas = dests == null ? 0
                : (int) dests.stream().filter(d -> Boolean.TRUE.equals(d.getLeida())).count();
        String destino = n.getSucursal() != null ? n.getSucursal().getNombre() : "Toda la empresa";
        return NotificacionEnviadaResponse.builder()
                .id(n.getId())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .categoria(n.getCategoria() != null ? n.getCategoria().name() : null)
                .prioridad(n.getPrioridad() != null ? n.getPrioridad().name() : null)
                .origen(n.getOrigen() != null ? n.getOrigen().name() : null)
                .destino(destino)
                .destinatarios(total)
                .leidas(leidas)
                .fecha(n.getFechaCreacion())
                .build();
    }
}
