package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.DocumentoMetaResponse;
import tipqr.back.dto.SolicitudDetalleResponse;
import tipqr.back.dto.SolicitudResumenResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.DocumentoRegistroRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperadminService {

    private final UsuarioRepository usuarioRepository;
    private final DocumentoRegistroRepository documentoRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<SolicitudResumenResponse> solicitudesPendientes() {
        return usuarioRepository
                .findByEstadoCuentaOrderByFechaCreacionDesc(EstadoCuenta.PENDIENTE_VALIDACION)
                .stream().map(SolicitudResumenResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudDetalleResponse detalle(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        List<DocumentoMetaResponse> docs = documentoRepository.findByUsuarioId(usuarioId)
                .stream().map(DocumentoMetaResponse::fromEntity).toList();
        return SolicitudDetalleResponse.build(usuario, docs);
    }

    @Transactional(readOnly = true)
    public DocumentoRegistro documento(Long documentoId) {
        return documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));
    }

    @Transactional
    public void aprobar(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setEstadoCuenta(EstadoCuenta.APROBADA);
        usuario.setEmailToken(null);
        usuarioRepository.save(usuario);
        emailService.enviarResultadoValidacion(usuario.getEmail(), usuario.getNombre(), true, null);
    }

    @Transactional
    public void rechazar(Long usuarioId, String motivo) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setEstadoCuenta(EstadoCuenta.RECHAZADA);
        usuarioRepository.save(usuario);
        emailService.enviarResultadoValidacion(usuario.getEmail(), usuario.getNombre(), false, motivo);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", usuarioId));
    }
}
