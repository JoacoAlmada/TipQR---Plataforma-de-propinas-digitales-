package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    /** Todas las empresas que administra el dueño (marca la activa). */
    @GetMapping("/mias")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<List<EmpresaResponse>> mias(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.misEmpresas(user.getUsername()));
    }

    /** Crea una empresa adicional y la deja activa. */
    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> crear(
            @Valid @RequestBody EmpresaRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(empresaService.crear(request, user.getUsername()));
    }

    /** Sube o reemplaza la constancia de AFIP de una empresa propia pendiente de validación. */
    @PostMapping("/{id}/constancia")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> subirConstancia(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.subirConstancia(id, user.getUsername(), archivo));
    }

    /** Reenvía a validación una empresa rechazada (tras corregir datos/constancia). */
    @PostMapping("/{id}/reenviar")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> reenviar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.reenviarValidacion(id, user.getUsername()));
    }

    /** Cambia la empresa que el dueño está gestionando. */
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> activar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.cambiarActiva(id, user.getUsername()));
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
