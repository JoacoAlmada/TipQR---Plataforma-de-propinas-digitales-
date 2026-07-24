package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.DocumentoMetaResponse;
import tipqr.back.dto.EmpresaValidacionResponse;
import tipqr.back.dto.SolicitudDetalleResponse;
import tipqr.back.dto.SolicitudResumenResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.entity.enums.EstadoValidacionEmpresa;
import tipqr.back.entity.enums.Rol;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.DocumentoRegistroRepository;
import tipqr.back.repository.EmpresaRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperadminService {

    private final UsuarioRepository usuarioRepository;
    private final DocumentoRegistroRepository documentoRepository;
    private final EmpresaRepository empresaRepository;
    private final EmailService emailService;
    private final NotificacionService notificacionService;

    /**
     * Cuentas de DUEÑO en un estado dado (por defecto, pendientes de validación). Solo los dueños
     * pasan por el auto-registro con documentos; los empleados/encargados no aparecen acá.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResumenResponse> solicitudes(EstadoCuenta estado) {
        EstadoCuenta filtro = estado != null ? estado : EstadoCuenta.PENDIENTE_VALIDACION;
        return usuarioRepository
                .findByRolAndEstadoCuentaOrderByFechaCreacionDesc(Rol.DUENO, filtro)
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
        usuario.setMotivoRechazo(null);
        usuarioRepository.save(usuario);
        emailService.enviarResultadoValidacion(usuario.getEmail(), usuario.getNombre(), true, null, null);
    }

    @Transactional
    public void rechazar(Long usuarioId, String motivo) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setEstadoCuenta(EstadoCuenta.RECHAZADA);
        usuario.setMotivoRechazo(motivo);
        usuarioRepository.save(usuario);
        // El token de registro se conserva para que pueda retomar el formulario y reenviar.
        emailService.enviarResultadoValidacion(
                usuario.getEmail(), usuario.getNombre(), false, motivo, usuario.getEmailToken());
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", usuarioId));
    }

    // ── Validación de empresas nuevas (alta por un dueño ya validado) ──

    /** Empresas en un estado de validación (por defecto, pendientes). */
    @Transactional(readOnly = true)
    public List<EmpresaValidacionResponse> empresas(EstadoValidacionEmpresa estado) {
        EstadoValidacionEmpresa filtro = estado != null ? estado : EstadoValidacionEmpresa.PENDIENTE;
        List<Empresa> lista = new ArrayList<>(
                empresaRepository.findByEstadoValidacionOrderByFechaCreacionDesc(filtro));
        // Las empresas previas a esta feature tienen estado nulo y se consideran aprobadas.
        if (filtro == EstadoValidacionEmpresa.APROBADA) {
            lista.addAll(empresaRepository.findByEstadoValidacionIsNullOrderByFechaCreacionDesc());
        }
        return lista.stream().map(EmpresaValidacionResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaValidacionResponse detalleEmpresa(Long empresaId) {
        return EmpresaValidacionResponse.fromEntity(buscarEmpresa(empresaId));
    }

    /** Empresa con su constancia cargada (para servir el PDF). */
    @Transactional(readOnly = true)
    public Empresa empresaConConstancia(Long empresaId) {
        Empresa empresa = buscarEmpresa(empresaId);
        if (empresa.getConstanciaDatos() == null || empresa.getConstanciaDatos().length == 0) {
            throw new ResourceNotFoundException("Constancia de la empresa", empresaId);
        }
        return empresa;
    }

    @Transactional
    public void aprobarEmpresa(Long empresaId) {
        Empresa empresa = buscarEmpresa(empresaId);
        empresa.setEstadoValidacion(EstadoValidacionEmpresa.APROBADA);
        empresa.setMotivoRechazo(null);
        empresaRepository.save(empresa);
        notificacionService.notificarValidacionEmpresa(empresa, true, null);
    }

    @Transactional
    public void rechazarEmpresa(Long empresaId, String motivo) {
        Empresa empresa = buscarEmpresa(empresaId);
        empresa.setEstadoValidacion(EstadoValidacionEmpresa.RECHAZADA);
        empresa.setMotivoRechazo(motivo);
        empresaRepository.save(empresa);
        notificacionService.notificarValidacionEmpresa(empresa, false, motivo);
    }

    private Empresa buscarEmpresa(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
    }
}
