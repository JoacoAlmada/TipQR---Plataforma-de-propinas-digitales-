package tipqr.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tipqr.back.dto.MiDocumentoResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.enums.TipoDocumento;
import tipqr.back.service.MiDocumentoService;

import java.util.List;

/**
 * Documentos del dueño (los del registro), consultables y reemplazables desde "Mi empresa".
 */
@RestController
@RequestMapping("/api/perfil/documentos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DUENO')")
public class MiDocumentoController {

    private final MiDocumentoService miDocumentoService;

    /** Estado de los documentos del dueño. */
    @GetMapping
    public ResponseEntity<List<MiDocumentoResponse>> mis(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(miDocumentoService.misDocumentos(user.getUsername()));
    }

    /** Binario de un documento (imagen o PDF) para previsualizar/descargar. */
    @GetMapping("/{tipo}/archivo")
    public ResponseEntity<byte[]> archivo(
            @PathVariable TipoDocumento tipo,
            @AuthenticationPrincipal UserDetails user) {
        DocumentoRegistro doc = miDocumentoService.archivo(user.getUsername(), tipo);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(doc.getDatos());
    }

    /** Sube o reemplaza un documento. */
    @PostMapping
    public ResponseEntity<MiDocumentoResponse> reemplazar(
            @RequestParam TipoDocumento tipo,
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(miDocumentoService.reemplazar(user.getUsername(), tipo, archivo));
    }
}
