package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<EmpresaResponse>> listar() {
        return ResponseEntity.ok(empresaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> crear(@Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.ok(empresaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EmpresaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado) {
        return ResponseEntity.ok(empresaService.cambiarEstado(id, estado));
    }
}
