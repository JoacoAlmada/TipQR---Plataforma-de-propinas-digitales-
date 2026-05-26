package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.CategoriaNotificacion;
import tipqr.back.entity.enums.OrigenNotificacion;
import tipqr.back.entity.enums.PrioridadNotificacion;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notificacion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id", nullable = false)
    private Usuario creadoPor;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    private CategoriaNotificacion categoria;

    @Enumerated(EnumType.STRING)
    private PrioridadNotificacion prioridad;

    @Enumerated(EnumType.STRING)
    private OrigenNotificacion origen;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "notificacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotificacionDestinatario> destinatarios;
}
