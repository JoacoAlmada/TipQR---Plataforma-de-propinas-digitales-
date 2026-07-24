package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tipqr.back.dto.MiDocumentoResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.TipoDocumento;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.DocumentoRegistroRepository;
import tipqr.back.repository.UsuarioRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Documentos del dueño (los que cargó en el registro), accesibles y editables desde "Mi empresa".
 */
@Service
@RequiredArgsConstructor
public class MiDocumentoService {

    private static final String TIPO_PDF = "application/pdf";
    private static final Set<String> TIPOS_IMAGEN = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    private final UsuarioRepository usuarioRepository;
    private final DocumentoRegistroRepository documentoRepository;

    /** Estado de los 4 documentos del usuario (cargados o pendientes). */
    @Transactional(readOnly = true)
    public List<MiDocumentoResponse> misDocumentos(String email) {
        Usuario usuario = usuario(email);
        return List.of(TipoDocumento.values()).stream()
                .map(tipo -> documentoRepository.findByUsuarioIdAndTipo(usuario.getId(), tipo)
                        .map(MiDocumentoResponse::de)
                        .orElseGet(() -> MiDocumentoResponse.vacio(tipo)))
                .toList();
    }

    /** Binario de un documento del usuario (para previsualizar/descargar). */
    @Transactional(readOnly = true)
    public DocumentoRegistro archivo(String email, TipoDocumento tipo) {
        Usuario usuario = usuario(email);
        return documentoRepository.findByUsuarioIdAndTipo(usuario.getId(), tipo)
                .orElseThrow(() -> new ResourceNotFoundException("No hay un documento " + tipo.name() + " cargado"));
    }

    /** Sube o reemplaza un documento del usuario. */
    @Transactional
    public MiDocumentoResponse reemplazar(String email, TipoDocumento tipo, MultipartFile archivo) {
        Usuario usuario = usuario(email);
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        validarTipoArchivo(tipo, archivo.getContentType());

        DocumentoRegistro doc = documentoRepository.findByUsuarioIdAndTipo(usuario.getId(), tipo)
                .orElseGet(DocumentoRegistro::new);
        doc.setUsuario(usuario);
        doc.setTipo(tipo);
        doc.setNombreArchivo(archivo.getOriginalFilename());
        doc.setContentType(archivo.getContentType());
        try {
            doc.setDatos(archivo.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo");
        }
        return MiDocumentoResponse.de(documentoRepository.save(doc));
    }

    // ── Helpers ─────────────────────────────────────────

    private void validarTipoArchivo(TipoDocumento tipo, String contentType) {
        if (tipo == TipoDocumento.CONSTANCIA_AFIP) {
            if (!TIPO_PDF.equals(contentType)) {
                throw new IllegalArgumentException("La constancia de AFIP debe ser un PDF");
            }
        } else if (contentType == null || !TIPOS_IMAGEN.contains(contentType)) {
            throw new IllegalArgumentException("El documento debe ser una imagen (JPG o PNG)");
        }
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
