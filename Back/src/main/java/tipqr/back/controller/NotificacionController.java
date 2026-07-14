package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.CrearNotificacionRequest;
import tipqr.back.dto.NotificacionResponse;
import tipqr.back.service.NotificacionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /** Envía una notificación a los empleados (dueño/encargado). */
    @PostMapping
    @PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
    public ResponseEntity<Map<String, Integer>> enviar(
            @Valid @RequestBody CrearNotificacionRequest request,
            @AuthenticationPrincipal UserDetails user) {
        int enviados = notificacionService.enviar(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("enviados", enviados));
    }

    /** Bandeja de notificaciones del usuario logueado. */
    @GetMapping("/mias")
    public ResponseEntity<List<NotificacionResponse>> mias(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(notificacionService.misNotificaciones(user.getUsername()));
    }

    /** Cantidad de notificaciones no leídas (para el badge). */
    @GetMapping("/mias/no-leidas")
    public ResponseEntity<Map<String, Long>> noLeidas(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas(user.getUsername())));
    }

    /** Marca una notificación como leída. */
    @PatchMapping("/mias/{id}/leida")
    public ResponseEntity<Void> marcarLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        notificacionService.marcarLeida(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
