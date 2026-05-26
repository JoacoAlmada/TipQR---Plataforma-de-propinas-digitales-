package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grupo_propina_empleado",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grupo_propina_id", "empleado_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GrupoPropinaEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_propina_id", nullable = false)
    private GrupoPropina grupoPropina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    private Double porcentajeDistribucion;

    @Builder.Default
    private Boolean activo = true;
}
