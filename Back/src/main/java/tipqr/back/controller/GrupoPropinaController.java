package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.AsignarEmpleadoRequest;
import tipqr.back.dto.GrupoPropinaRequest;
import tipqr.back.dto.GrupoPropinaResponse;
import tipqr.back.dto.MiembroGrupoResponse;
import tipqr.back.service.GrupoPropinaService;

import java.util.List;

@RestController
@RequestMapping("/api/grupos-propina")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
public class GrupoPropinaController {

    private final GrupoPropinaService grupoService;

    @GetMapping
    public ResponseEntity<List<GrupoPropinaResponse>> listar(
            @RequestParam(required = false) Long sucursalId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(grupoService.listar(user.getUsername(), sucursalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoPropinaResponse> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(grupoService.obtenerPorId(id, user.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<GrupoPropinaResponse> crear(
            @Valid @RequestBody GrupoPropinaRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(grupoService.crear(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<GrupoPropinaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GrupoPropinaRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(grupoService.actualizar(id, request, user.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<GrupoPropinaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(grupoService.cambiarEstado(id, estado, user.getUsername()));
    }

    // ── Miembros del grupo ──────────────────────────────

    @GetMapping("/{id}/empleados")
    public ResponseEntity<List<MiembroGrupoResponse>> miembros(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(grupoService.listarMiembros(id, user.getUsername()));
    }

    @PostMapping("/{id}/empleados")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<Void> agregarEmpleado(
            @PathVariable Long id,
            @Valid @RequestBody AsignarEmpleadoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        grupoService.agregarEmpleado(id, request.getEmpleadoId(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/empleados/{empleadoId}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<Void> removerEmpleado(
            @PathVariable Long id,
            @PathVariable Long empleadoId,
            @AuthenticationPrincipal UserDetails user) {
        grupoService.removerEmpleado(id, empleadoId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
