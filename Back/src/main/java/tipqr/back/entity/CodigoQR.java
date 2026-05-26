package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.TipoDestinoQR;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigo_qr")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CodigoQR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDestinoQR tipoDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_propina_id")
    private GrupoPropina grupoPropina;

    private String url;

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;
}
