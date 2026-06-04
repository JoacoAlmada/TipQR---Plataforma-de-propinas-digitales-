package tipqr.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.SolicitudDetalleResponse;
import tipqr.back.dto.SolicitudResumenResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.service.SuperadminService;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperadminController {

    private final SuperadminService superadminService;

    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudResumenResponse>> solicitudes() {
        return ResponseEntity.ok(superadminService.solicitudesPendientes());
    }

    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<SolicitudDetalleResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(superadminService.detalle(id));
    }

    @GetMapping("/documentos/{docId}")
    public ResponseEntity<byte[]> descargarDocumento(@PathVariable Long docId) {
        DocumentoRegistro doc = superadminService.documento(docId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getNombreArchivo() + "\"")
                .body(doc.getDatos());
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    public ResponseEntity<Void> aprobar(@PathVariable Long id) {
        superadminService.aprobar(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    public ResponseEntity<Void> rechazar(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        superadminService.rechazar(id, motivo);
        return ResponseEntity.ok().build();
    }
}
