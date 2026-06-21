package tipqr.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tipqr.back.dto.QrResponse;
import tipqr.back.service.QrService;

import java.util.List;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DUENO', 'ENCARGADO')")
public class QrController {

    private final QrService qrService;

    @GetMapping
    public ResponseEntity<List<QrResponse>> listar(
            @RequestParam(required = false) Long sucursalId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(qrService.listar(user.getUsername(), sucursalId));
    }

    @GetMapping(value = "/{id}/imagen", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> imagen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        byte[] png = qrService.obtenerImagenPng(id, user.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-" + id + ".png\"")
                .body(png);
    }

    @PostMapping("/{id}/regenerar")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<QrResponse> regenerar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(qrService.regenerar(id, user.getUsername()));
    }
}
