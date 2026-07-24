package tipqr.back.dto;

import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.enums.TipoDocumento;

import java.time.format.DateTimeFormatter;

/** Estado de un documento del dueño en "Mi empresa" (metadatos, sin el binario). */
@Getter
@Builder
public class MiDocumentoResponse {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String tipo;
    private boolean cargado;
    private String nombreArchivo;
    private String contentType;
    private boolean esPdf;
    private String fechaCarga;

    public static MiDocumentoResponse de(DocumentoRegistro d) {
        return MiDocumentoResponse.builder()
                .tipo(d.getTipo().name())
                .cargado(true)
                .nombreArchivo(d.getNombreArchivo())
                .contentType(d.getContentType())
                .esPdf("application/pdf".equals(d.getContentType()))
                .fechaCarga(d.getFechaCarga() != null ? d.getFechaCarga().format(FMT) : null)
                .build();
    }

    public static MiDocumentoResponse vacio(TipoDocumento tipo) {
        return MiDocumentoResponse.builder()
                .tipo(tipo.name())
                .cargado(false)
                .esPdf(tipo == TipoDocumento.CONSTANCIA_AFIP)
                .build();
    }
}
