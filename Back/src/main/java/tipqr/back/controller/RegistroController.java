package tipqr.back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tipqr.back.dto.RegistroDatosRequest;
import tipqr.back.dto.RegistroEstadoResponse;
import tipqr.back.dto.RegistroPaso1Request;
import tipqr.back.dto.RegistroPaso2Request;
import tipqr.back.dto.RegistroResumenResponse;
import tipqr.back.entity.enums.TipoDocumento;
import tipqr.back.service.RegistroService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/paso1")
    public ResponseEntity<RegistroEstadoResponse> paso1(@Valid @RequestBody RegistroPaso1Request request) {
        String token = registroService.paso1(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistroEstadoResponse(token, "CREADA", false));
    }

    /** Link del email. Verifica y redirige al frontend. */
    @GetMapping("/verificar")
    public ResponseEntity<Void> verificar(@RequestParam String token) {
        String destino;
        try {
            registroService.verificarEmail(token);
            destino = frontendUrl + "/verificar-email?ok=true";
        } catch (Exception e) {
            destino = frontendUrl + "/verificar-email?ok=false";
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(destino)).build();
    }

    /** El wizard consulta este endpoint para saber si el email ya se verificó. */
    @GetMapping("/estado")
    public ResponseEntity<RegistroEstadoResponse> estado(@RequestParam String token) {
        return ResponseEntity.ok(registroService.estado(token));
    }

    /** Datos completos del registro para retomarlo tras un rechazo. */
    @GetMapping("/resumen")
    public ResponseEntity<RegistroResumenResponse> resumen(@RequestParam String token) {
        return ResponseEntity.ok(registroService.resumen(token));
    }

    /** Corrige los datos personales del dueño al retomar el registro. */
    @PutMapping("/datos")
    public ResponseEntity<Void> actualizarDatos(
            @RequestParam String token,
            @Valid @RequestBody RegistroDatosRequest request) {
        registroService.actualizarDatosPersonales(token, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/paso2")
    public ResponseEntity<Void> paso2(
            @RequestParam String token,
            @Valid @RequestBody RegistroPaso2Request request) {
        registroService.paso2(token, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/documentos")
    public ResponseEntity<Void> subirDocumento(
            @RequestParam String token,
            @RequestParam TipoDocumento tipo,
            @RequestParam("archivo") MultipartFile archivo) {
        registroService.subirDocumento(token, tipo, archivo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/documentos")
    public ResponseEntity<List<TipoDocumento>> documentos(@RequestParam String token) {
        return ResponseEntity.ok(registroService.documentosCargados(token));
    }

    @PostMapping("/finalizar")
    public ResponseEntity<Void> finalizar(@RequestParam String token) {
        registroService.finalizar(token);
        return ResponseEntity.ok().build();
    }
}
