package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.TipoReporte;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_automatico")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReporteAutomatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    private TipoReporte tipoReporte;

    private LocalDate periodoDesde;

    private LocalDate periodoHasta;

    @Column(columnDefinition = "TEXT")
    private String resumenGenerado;

    @CreationTimestamp
    private LocalDateTime fechaGeneracion;
}
