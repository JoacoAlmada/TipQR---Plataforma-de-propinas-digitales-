package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.MesaRequest;
import tipqr.back.dto.MesaResponse;
import tipqr.back.service.MesaService;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    public ResponseEntity<List<MesaResponse>> listar(
            @RequestParam(required = false) Long sucursalId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(mesaService.listar(user.getUsername(), sucursalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponse> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(mesaService.obtenerPorId(id, user.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<MesaResponse> crear(
            @Valid @RequestBody MesaRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mesaService.crear(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<MesaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MesaRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(mesaService.actualizar(id, request, user.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<MesaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(mesaService.cambiarEstado(id, estado, user.getUsername()));
    }
}
