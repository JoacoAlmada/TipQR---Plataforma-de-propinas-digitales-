package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "grupo_propina")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GrupoPropina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private String tipoGrupo;

    @Builder.Default
    private Boolean estado = true;

    @OneToMany(mappedBy = "grupoPropina", fetch = FetchType.LAZY)
    private List<GrupoPropinaEmpleado> miembros;
}
