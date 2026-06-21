package tipqr.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tipqr.back.dto.OrdenEstadoResponse;
import tipqr.back.service.OrdenService;

/**
 * Endpoints públicos de órdenes de propina (sin login).
 * La pantalla pública consulta el estado por el código mientras espera el pago.
 */
@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    @GetMapping("/{codigo}/estado")
    public ResponseEntity<OrdenEstadoResponse> estado(@PathVariable String codigo) {
        return ResponseEntity.ok(ordenService.consultarEstadoPublico(codigo));
    }
}
