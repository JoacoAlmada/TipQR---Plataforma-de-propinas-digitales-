package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.EstadoValidacionEmpresa;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "empresa")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String nombreFantasia;

    private String rubro;

    @Column(unique = true)
    private String cuit;

    private String provincia;

    private String calle;

    private String numeracion;

    private String emailContacto;

    private String telefono;

    @Builder.Default
    private Boolean estado = true;

    /**
     * Validación de la empresa. Por defecto APROBADA (registro, seed y las que ya operaban);
     * las que da de alta un dueño desde "Mi empresa" nacen PENDIENTE hasta que el superadmin
     * las revise.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoValidacionEmpresa estadoValidacion = EstadoValidacionEmpresa.APROBADA;

    /** Motivo cuando el superadmin la rechaza. */
    @Column(columnDefinition = "TEXT")
    private String motivoRechazo;

    // Constancia de AFIP de la empresa (para las altas que requieren validación).
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] constanciaDatos;

    private String constanciaNombre;

    private String constanciaContentType;

    /** Dueño propietario de la empresa (un dueño puede tener varias). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_usuario_id")
    private Usuario propietario;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Sucursal> sucursales;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Usuario> usuarios;
}
