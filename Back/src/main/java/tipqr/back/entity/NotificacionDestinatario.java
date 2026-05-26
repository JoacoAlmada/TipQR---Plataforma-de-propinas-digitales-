package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion_destinatario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificacionDestinatario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private Notificacion notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Builder.Default
    private Boolean leida = false;

    private LocalDateTime fechaLectura;
}
