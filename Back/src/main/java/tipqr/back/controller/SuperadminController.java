package tipqr.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.EmpresaValidacionResponse;
import tipqr.back.dto.SolicitudDetalleResponse;
import tipqr.back.dto.SolicitudResumenResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.entity.enums.EstadoValidacionEmpresa;
import tipqr.back.service.SuperadminService;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperadminController {

    private final SuperadminService superadminService;

    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudResumenResponse>> solicitudes(
            @RequestParam(required = false) EstadoCuenta estado) {
        return ResponseEntity.ok(superadminService.solicitudes(estado));
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

    // ── Validación de empresas nuevas ──

    @GetMapping("/empresas")
    public ResponseEntity<List<EmpresaValidacionResponse>> empresas(
            @RequestParam(required = false) EstadoValidacionEmpresa estado) {
        return ResponseEntity.ok(superadminService.empresas(estado));
    }

    @GetMapping("/empresas/{id}")
    public ResponseEntity<EmpresaValidacionResponse> detalleEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(superadminService.detalleEmpresa(id));
    }

    @GetMapping("/empresas/{id}/constancia")
    public ResponseEntity<byte[]> constanciaEmpresa(@PathVariable Long id) {
        Empresa empresa = superadminService.empresaConConstancia(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(empresa.getConstanciaContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + empresa.getConstanciaNombre() + "\"")
                .body(empresa.getConstanciaDatos());
    }

    @PostMapping("/empresas/{id}/aprobar")
    public ResponseEntity<Void> aprobarEmpresa(@PathVariable Long id) {
        superadminService.aprobarEmpresa(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/empresas/{id}/rechazar")
    public ResponseEntity<Void> rechazarEmpresa(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        superadminService.rechazarEmpresa(id, motivo);
        return ResponseEntity.ok().build();
    }
}
