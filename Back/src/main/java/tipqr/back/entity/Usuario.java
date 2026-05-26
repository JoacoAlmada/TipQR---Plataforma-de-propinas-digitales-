package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.Rol;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Builder.Default
    private Boolean estado = true;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Empleado empleado;
}
