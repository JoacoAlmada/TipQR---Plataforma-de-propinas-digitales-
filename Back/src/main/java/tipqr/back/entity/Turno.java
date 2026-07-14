package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Turno de trabajo de una sucursal. Define el GRUPO de propina "activo" mientras está abierto:
 * las propinas de mesa de esa sucursal se reparten entre los integrantes de ese grupo.
 * Solo hay un turno activo por sucursal a la vez (abrir uno nuevo cierra el anterior).
 */
@Entity
@Table(name = "turno")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_propina_id", nullable = false)
    private GrupoPropina grupoPropina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abierto_por_usuario_id")
    private Usuario abiertoPor;

    /** Etiqueta opcional del turno (ej: "Mañana", "Noche"). */
    private String nombre;

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;
}
