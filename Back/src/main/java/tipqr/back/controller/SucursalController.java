package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.SucursalRequest;
import tipqr.back.dto.SucursalResponse;
import tipqr.back.service.SucursalService;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
public class SucursalController {

    private final SucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<SucursalResponse>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(sucursalService.listar(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponse> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(sucursalService.obtenerPorId(id, user.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<SucursalResponse> crear(
            @Valid @RequestBody SucursalRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sucursalService.crear(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<SucursalResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SucursalRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(sucursalService.actualizar(id, request, user.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<SucursalResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(sucursalService.cambiarEstado(id, estado, user.getUsername()));
    }
}
