package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.EmpresaRequest;
import tipqr.back.dto.EmpresaResponse;
import tipqr.back.service.EmpresaService;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.listar(user.getUsername()));
    }

    @GetMapping("/mia")
    public ResponseEntity<EmpresaResponse> miEmpresa(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.miEmpresa(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponse> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.obtenerPorId(id, user.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.actualizar(id, request, user.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.cambiarEstado(id, estado, user.getUsername()));
    }
}
