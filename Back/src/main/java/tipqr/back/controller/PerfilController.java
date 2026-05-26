package tipqr.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tipqr.back.entity.Usuario;
import tipqr.back.repository.UsuarioRepository;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioRepository usuarioRepository;

    /**
     * Accesible por cualquier usuario autenticado.
     * Devuelve los datos del usuario logueado.
     */
    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> miPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(Map.of(
                "email", usuario.getEmail(),
                "nombre", usuario.getNombre(),
                "apellido", usuario.getApellido(),
                "rol", usuario.getRol().name()
        ));
    }

    /**
     * Solo accesible por DUENO o ENCARGADO.
     */
    @GetMapping("/admin/perfil")
    @PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
    public ResponseEntity<Map<String, Object>> perfilAdmin(
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(Map.of(
                "email", usuario.getEmail(),
                "nombre", usuario.getNombre(),
                "apellido", usuario.getApellido(),
                "rol", usuario.getRol().name(),
                "acceso", "Panel administrativo"
        ));
    }

    /**
     * Solo accesible por EMPLEADO.
     */
    @GetMapping("/empleado/perfil")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<Map<String, Object>> perfilEmpleado(
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(Map.of(
                "email", usuario.getEmail(),
                "nombre", usuario.getNombre(),
                "apellido", usuario.getApellido(),
                "rol", usuario.getRol().name(),
                "acceso", "Panel empleado"
        ));
    }
}
