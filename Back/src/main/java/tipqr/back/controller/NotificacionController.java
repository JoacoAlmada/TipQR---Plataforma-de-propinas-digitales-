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
import tipqr.back.dto.NotificacionEnviadaResponse;
import tipqr.back.dto.NotificacionResponse;
import tipqr.back.dto.RedaccionNotificacionResponse;
import tipqr.back.dto.RedactarNotificacionRequest;
import tipqr.back.service.NotificacionService;
import tipqr.back.service.ia.AgenteNotificacionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final AgenteNotificacionService agenteNotificacionService;

    /** Envía una notificación a los empleados (dueño/encargado). */
    @PostMapping
    @PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
    public ResponseEntity<Map<String, Integer>> enviar(
            @Valid @RequestBody CrearNotificacionRequest request,
            @AuthenticationPrincipal UserDetails user) {
        int enviados = notificacionService.enviar(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("enviados", enviados));
    }

    /**
     * Redacta un borrador de aviso con el agente de IA a partir de una instrucción informal.
     * No envía nada: el usuario revisa/edita y confirma con POST /api/notificaciones.
     */
    @PostMapping("/redactar-ia")
    @PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
    public ResponseEntity<RedaccionNotificacionResponse> redactarIa(
            @Valid @RequestBody RedactarNotificacionRequest request) {
        return ResponseEntity.ok(agenteNotificacionService.redactar(request.getInstruccion()));
    }

    /** Bandeja de notificaciones del usuario logueado. */
    @GetMapping("/mias")
    public ResponseEntity<List<NotificacionResponse>> mias(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(notificacionService.misNotificaciones(user.getUsername()));
    }

    /** Notificaciones enviadas por el usuario (dueño/encargado), con estadística de lectura. */
    @GetMapping("/enviadas")
    @PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
    public ResponseEntity<List<NotificacionEnviadaResponse>> enviadas(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(notificacionService.notificacionesEnviadas(user.getUsername()));
    }

    /** Elimina una notificación enviada (la borra para todos los destinatarios). */
    @DeleteMapping("/enviadas/{id}")
    @PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
    public ResponseEntity<Void> eliminarEnviada(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        notificacionService.eliminarEnviada(id, user.getUsername());
        return ResponseEntity.noContent().build();
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

    /** Elimina la notificación de la bandeja del usuario. */
    @DeleteMapping("/mias/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        notificacionService.eliminarDeBandeja(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
