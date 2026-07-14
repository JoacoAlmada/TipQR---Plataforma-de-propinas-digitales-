package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Notificacion;
import tipqr.back.entity.NotificacionDestinatario;

import java.time.LocalDateTime;

/** Notificación tal como la ve un destinatario en su bandeja. */
@Getter
@Builder
@AllArgsConstructor
public class NotificacionResponse {

    private Long id;            // id del NotificacionDestinatario (para marcar leída)
    private String titulo;
    private String mensaje;
    private String categoria;
    private String prioridad;
    private String origen;
    private String emisor;
    private boolean leida;
    private LocalDateTime fecha;

    public static NotificacionResponse fromEntity(NotificacionDestinatario d) {
        Notificacion n = d.getNotificacion();
        String emisor = n.getCreadoPor() != null
                ? (n.getCreadoPor().getNombre() + " " +
                   (n.getCreadoPor().getApellido() != null ? n.getCreadoPor().getApellido() : "")).trim()
                : "TipQR";
        return NotificacionResponse.builder()
                .id(d.getId())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .categoria(n.getCategoria() != null ? n.getCategoria().name() : null)
                .prioridad(n.getPrioridad() != null ? n.getPrioridad().name() : null)
                .origen(n.getOrigen() != null ? n.getOrigen().name() : null)
                .emisor(emisor)
                .leida(Boolean.TRUE.equals(d.getLeida()))
                .fecha(n.getFechaCreacion())
                .build();
    }
}
