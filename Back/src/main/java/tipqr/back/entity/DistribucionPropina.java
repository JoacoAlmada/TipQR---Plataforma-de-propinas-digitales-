package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "distribucion_propina")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DistribucionPropina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_propina_id", nullable = false)
    private OrdenPropina ordenPropina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoAsignado;

    private Double porcentaje;

    private String criterio;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;
}
