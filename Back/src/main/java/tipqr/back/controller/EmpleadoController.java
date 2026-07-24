package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tipqr.back.dto.EmpleadoRequest;
import tipqr.back.dto.EmpleadoResponse;
import tipqr.back.dto.MiDocumentoResponse;
import tipqr.back.dto.SucursalResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.enums.TipoDocumento;
import tipqr.back.service.EmpleadoService;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public ResponseEntity<List<EmpleadoResponse>> listar(
            @RequestParam(required = false) Long sucursalId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.listar(user.getUsername(), sucursalId));
    }

    /** Sucursal del usuario logueado (para el panel del encargado). */
    @GetMapping("/mi-sucursal")
    public ResponseEntity<SucursalResponse> miSucursal(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.miSucursal(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.obtenerPorId(id, user.getUsername()));
    }

    @PatchMapping("/{id}/encargado")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpleadoResponse> marcarEncargado(
            @PathVariable Long id,
            @RequestParam boolean valor,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.marcarEncargado(id, valor, user.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpleadoResponse> crear(
            @Valid @RequestBody EmpleadoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empleadoService.crear(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpleadoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.actualizar(id, request, user.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpleadoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.cambiarEstado(id, estado, user.getUsername()));
    }

    // ── Documentación del empleado (DNI frente/dorso, selfie) ──

    /** Estado de los documentos del empleado. */
    @GetMapping("/{id}/documentos")
    public ResponseEntity<List<MiDocumentoResponse>> documentos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.documentos(id, user.getUsername()));
    }

    /** Binario de una foto del empleado para previsualizar. */
    @GetMapping("/{id}/documentos/{tipo}/archivo")
    public ResponseEntity<byte[]> archivo(
            @PathVariable Long id,
            @PathVariable TipoDocumento tipo,
            @AuthenticationPrincipal UserDetails user) {
        DocumentoRegistro doc = empleadoService.archivo(id, tipo, user.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(doc.getDatos());
    }

    /** Sube o reemplaza una foto del empleado. */
    @PostMapping("/{id}/documentos")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<MiDocumentoResponse> subirDocumento(
            @PathVariable Long id,
            @RequestParam TipoDocumento tipo,
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empleadoService.subirDocumento(id, tipo, archivo, user.getUsername()));
    }
}
