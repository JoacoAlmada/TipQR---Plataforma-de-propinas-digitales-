package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.CrearNotificacionRequest;
import tipqr.back.dto.NotificacionResponse;
import tipqr.back.entity.*;
import tipqr.back.entity.enums.CategoriaNotificacion;
import tipqr.back.entity.enums.OrigenNotificacion;
import tipqr.back.entity.enums.PrioridadNotificacion;
import tipqr.back.entity.enums.Rol;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Notificaciones internas: envío manual (dueño/encargado) y avisos automáticos del sistema
 * (ej. "recibiste una propina"). Cada notificación genera un registro por destinatario.
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final NotificacionDestinatarioRepository destinatarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;

    // ── Envío manual ──────────────────────────────────────────────────────────

    /** Crea y envía una notificación a los empleados de una sucursal o de toda la empresa. */
    @Transactional
    public int enviar(CrearNotificacionRequest req, String emailEmisor) {
        Usuario emisor = usuario(emailEmisor);
        Empresa empresa = empresaDe(emisor);

        Sucursal sucursal = null;
        List<Empleado> destinatarios;
        if (req.getSucursalId() != null) {
            sucursal = sucursalRepository.findByIdAndEmpresaId(req.getSucursalId(), empresa.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal", req.getSucursalId()));
            destinatarios = empleadoRepository.findBySucursalIdOrderByNombreVisibleAsc(sucursal.getId());
        } else {
            destinatarios = empleadoRepository.findBySucursal_Empresa_IdOrderByNombreVisibleAsc(empresa.getId());
        }

        Notificacion notif = notificacionRepository.save(Notificacion.builder()
                .empresa(empresa)
                .sucursal(sucursal)
                .creadoPor(emisor)
                .titulo(req.getTitulo().trim())
                .mensaje(req.getMensaje().trim())
                .categoria(req.getCategoria() != null ? req.getCategoria() : CategoriaNotificacion.GENERAL)
                .prioridad(req.getPrioridad() != null ? req.getPrioridad() : PrioridadNotificacion.MEDIA)
                .origen(req.isAsistidoPorIa() ? OrigenNotificacion.AGENTE : OrigenNotificacion.MANUAL)
                .build());

        int enviados = 0;
        for (Empleado e : destinatarios) {
            if (e.getUsuario() == null) continue;
            crearDestinatario(notif, e.getUsuario());
            enviados++;
        }
        return enviados;
    }

    // ── Aviso automático del sistema ──────────────────────────────────────────

    /** Notifica a un empleado que recibió una propina (individual o su parte grupal). */
    @Transactional
    public void notificarPropinaRecibida(Empleado empleado, BigDecimal monto, String detalle) {
        if (empleado == null || empleado.getUsuario() == null || empleado.getSucursal() == null) {
            return;
        }
        Empresa empresa = empleado.getSucursal().getEmpresa();
        Usuario emisor = usuarioRepository
                .findFirstByEmpresa_IdAndRolOrderByIdAsc(empresa.getId(), Rol.DUENO)
                .orElse(empleado.getUsuario());

        Notificacion notif = notificacionRepository.save(Notificacion.builder()
                .empresa(empresa)
                .sucursal(empleado.getSucursal())
                .creadoPor(emisor)
                .titulo("¡Recibiste una propina!")
                .mensaje("Te dejaron $" + monto + (detalle != null ? " " + detalle : "") + ". ¡Buen trabajo!")
                .categoria(CategoriaNotificacion.PAGOS)
                .prioridad(PrioridadNotificacion.MEDIA)
                .origen(OrigenNotificacion.SISTEMA)
                .build());
        crearDestinatario(notif, empleado.getUsuario());
    }

    // ── Bandeja del usuario ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificacionResponse> misNotificaciones(String emailUsuario) {
        Usuario u = usuario(emailUsuario);
        return destinatarioRepository
                .findByUsuarioIdOrderByLeidaAscNotificacion_FechaCreacionDesc(u.getId())
                .stream().map(NotificacionResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String emailUsuario) {
        return destinatarioRepository.countByUsuarioIdAndLeidaFalse(usuario(emailUsuario).getId());
    }

    @Transactional
    public void marcarLeida(Long destinatarioId, String emailUsuario) {
        Usuario u = usuario(emailUsuario);
        NotificacionDestinatario d = destinatarioRepository
                .findByIdAndUsuarioId(destinatarioId, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", destinatarioId));
        if (!Boolean.TRUE.equals(d.getLeida())) {
            d.setLeida(true);
            d.setFechaLectura(LocalDateTime.now());
            destinatarioRepository.save(d);
        }
    }

    // ── Helpers ─────────────────────────────────────────

    private void crearDestinatario(Notificacion notif, Usuario usuario) {
        destinatarioRepository.save(NotificacionDestinatario.builder()
                .notificacion(notif)
                .usuario(usuario)
                .leida(false)
                .build());
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Empresa empresaDe(Usuario usuario) {
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }
}
