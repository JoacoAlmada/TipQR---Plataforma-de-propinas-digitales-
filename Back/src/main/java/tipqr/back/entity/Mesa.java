package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mesa",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sucursal_id", "numero"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private Integer numero;

    private String descripcion;

    @Builder.Default
    private Boolean estado = true;
}
