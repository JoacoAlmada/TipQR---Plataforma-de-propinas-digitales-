package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.TipoEventoOrden;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_orden")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EventoOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_propina_id", nullable = false)
    private OrdenPropina ordenPropina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoOrden tipoEvento;

    private String descripcion;

    @CreationTimestamp
    private LocalDateTime fecha;
}
