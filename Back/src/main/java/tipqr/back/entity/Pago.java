package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pago")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_propina_id", nullable = false, unique = true)
    private OrdenPropina ordenPropina;

    @Builder.Default
    private String proveedor = "MERCADO_PAGO";

    private String externalPaymentId;

    private String preferenceId;

    private String initPoint;

    private String estadoProveedor;

    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    @Builder.Default
    private String moneda = "ARS";

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WebhookPago> webhooks;
}
