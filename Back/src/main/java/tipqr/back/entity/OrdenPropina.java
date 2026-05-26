package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.TipoPropina;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orden_propina")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrdenPropina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_propina_id")
    private GrupoPropina grupoPropina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPropina tipoPropina;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaExpiracion;

    private LocalDateTime fechaPago;

    @OneToOne(mappedBy = "ordenPropina", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Pago pago;

    @OneToMany(mappedBy = "ordenPropina", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventoOrden> eventos;

    @OneToMany(mappedBy = "ordenPropina", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DistribucionPropina> distribuciones;
}
