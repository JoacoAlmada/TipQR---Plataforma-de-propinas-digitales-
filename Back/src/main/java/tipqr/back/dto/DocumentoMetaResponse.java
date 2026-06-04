package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tipqr.back.entity.DocumentoRegistro;

@Getter
@AllArgsConstructor
public class DocumentoMetaResponse {

    private Long id;
    private String tipo;
    private String nombreArchivo;
    private String contentType;

    public static DocumentoMetaResponse fromEntity(DocumentoRegistro d) {
        return new DocumentoMetaResponse(d.getId(), d.getTipo().name(), d.getNombreArchivo(), d.getContentType());
    }
}
