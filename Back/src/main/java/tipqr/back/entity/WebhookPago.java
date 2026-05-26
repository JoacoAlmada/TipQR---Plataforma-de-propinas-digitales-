package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_pago")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WebhookPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    private String proveedor;

    private String tipoEvento;

    private String externalId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    private LocalDateTime fechaRecepcion;

    @Builder.Default
    private Boolean procesado = false;
}
