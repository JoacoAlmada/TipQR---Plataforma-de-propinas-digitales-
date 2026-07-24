package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.TurnoAbrirRequest;
import tipqr.back.dto.TurnoResponse;
import tipqr.back.service.TurnoService;

import java.util.List;

/**
 * Turno activo de la sucursal. Lo gestionan el dueño y el encargado.
 */
@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
public class TurnoController {

    private final TurnoService turnoService;

    /** Turno activo de una sucursal (puede devolver vacío si no hay ninguno abierto). */
    @GetMapping("/activo")
    public ResponseEntity<TurnoResponse> activo(
            @RequestParam Long sucursalId,
            @AuthenticationPrincipal UserDetails user) {
        TurnoResponse turno = turnoService.turnoActivo(sucursalId, user.getUsername());
        return turno != null ? ResponseEntity.ok(turno) : ResponseEntity.noContent().build();
    }

    /** Turnos activos de todas las sucursales de la empresa (para segmentar avisos por turno). */
    @GetMapping("/activos")
    public ResponseEntity<List<TurnoResponse>> activos(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(turnoService.turnosActivos(user.getUsername()));
    }

    /** Abre un turno (eligiendo el grupo activo); cierra el anterior si lo hubiera. */
    @PostMapping("/abrir")
    public ResponseEntity<TurnoResponse> abrir(
            @Valid @RequestBody TurnoAbrirRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(turnoService.abrirTurno(request, user.getUsername()));
    }

    /** Cierra el turno activo de la sucursal. */
    @PostMapping("/cerrar")
    public ResponseEntity<TurnoResponse> cerrar(
            @RequestParam Long sucursalId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(turnoService.cerrarTurnoActivo(sucursalId, user.getUsername()));
    }
}
