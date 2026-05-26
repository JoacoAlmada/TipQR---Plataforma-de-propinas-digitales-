package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "empleado")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private String nombreVisible;

    private String puesto;

    @Builder.Default
    private Boolean estado = true;

    @CreationTimestamp
    private LocalDateTime fechaAlta;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY)
    private List<GrupoPropinaEmpleado> grupos;
}
