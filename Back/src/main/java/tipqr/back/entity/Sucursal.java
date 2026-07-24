package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sucursal")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String nombre;

    private String direccion;

    /** Coordenadas de la dirección (elegidas en el mapa). */
    private Double latitud;

    private Double longitud;

    private String telefono;

    @Builder.Default
    private Boolean estado = true;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "sucursal", fetch = FetchType.LAZY)
    private List<Empleado> empleados;

    @OneToMany(mappedBy = "sucursal", fetch = FetchType.LAZY)
    private List<Mesa> mesas;

    @OneToMany(mappedBy = "sucursal", fetch = FetchType.LAZY)
    private List<GrupoPropina> gruposPropina;
}
