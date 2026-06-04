package tipqr.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tipqr.back.entity.enums.TipoDocumento;

import java.time.LocalDateTime;

/**
 * Documento adjunto por el dueño en el paso 3 del registro (DNI frente/dorso,
 * selfie, constancia AFIP). El contenido binario se guarda en la base.
 */
@Entity
@Table(name = "documento_registro")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DocumentoRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(nullable = false)
    private String contentType;

    @Lob
    @Column(nullable = false)
    private byte[] datos;

    @CreationTimestamp
    private LocalDateTime fechaCarga;
}
