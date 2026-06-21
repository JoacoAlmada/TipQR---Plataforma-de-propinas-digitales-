package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.CrearOrdenPublicaRequest;
import tipqr.back.dto.OrdenEstadoResponse;
import tipqr.back.dto.QrDestinoResponse;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.service.OrdenService;
import tipqr.back.service.QrService;

/**
 * Endpoints públicos de la pantalla del cliente (sin login): escanear QR y dejar propina.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final QrService qrService;
    private final OrdenService ordenService;

    /** Resuelve el QR escaneado para mostrar a quién se le deja la propina. */
    @GetMapping("/qr/{codigo}")
    public ResponseEntity<QrDestinoResponse> resolverQr(@PathVariable String codigo) {
        return ResponseEntity.ok(qrService.resolverPublico(codigo));
    }

    /** Crea la orden de propina con el monto elegido por el cliente. */
    @PostMapping("/qr/{codigo}/ordenes")
    public ResponseEntity<OrdenEstadoResponse> crearOrden(
            @PathVariable String codigo,
            @Valid @RequestBody CrearOrdenPublicaRequest request) {
        OrdenPropina orden = ordenService.crearDesdeQr(codigo, request.getMonto());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdenEstadoResponse.fromEntity(orden));
    }
}
