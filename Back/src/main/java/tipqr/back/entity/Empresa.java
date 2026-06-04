package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Sucursal> sucursales;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Usuario> usuarios;
}
