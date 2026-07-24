package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.CrearNotificacionRequest;
import tipqr.back.dto.NotificacionEnviadaResponse;
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
    private final TurnoRepository turnoRepository;
    private final GrupoPropinaEmpleadoRepository miembroRepository;

    // ── Envío manual ──────────────────────────────────────────────────────────

    /** Crea y envía una notificación a los empleados de una sucursal o de toda la empresa. */
    @Transactional
    public int enviar(CrearNotificacionRequest req, String emailEmisor) {
        Usuario emisor = usuario(emailEmisor);
        Empresa empresa = empresaDe(emisor);

        // Segmentación del destinatario: se aplica la primera opción informada
        // (turno → rol → sucursal → toda la empresa).
        Sucursal sucursal = null;
        List<Empleado> destinatarios;
        if (req.getTurnoId() != null) {
            Turno turno = turnoRepository.findByIdAndSucursal_Empresa_Id(req.getTurnoId(), empresa.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno", req.getTurnoId()));
            sucursal = turno.getSucursal();
            destinatarios = miembroRepository
                    .findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(turno.getGrupoPropina().getId())
                    .stream()
                    .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                    .map(GrupoPropinaEmpleado::getEmpleado)
                    .toList();
        } else if (req.getRol() != null) {
            destinatarios = empleadoRepository
                    .findBySucursal_Empresa_IdAndUsuario_RolOrderByNombreVisibleAsc(empresa.getId(), req.getRol());
        } else if (req.getSucursalId() != null) {
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

    /** Avisa al dueño el resultado de la validación de una empresa que dio de alta. */
    @Transactional
    public void notificarValidacionEmpresa(Empresa empresa, boolean aprobada, String motivo) {
        Usuario duenio = empresa.getPropietario();
        if (duenio == null) return;

        String titulo = aprobada ? "Empresa aprobada ✔" : "Empresa rechazada";
        String mensaje = aprobada
                ? "Tu empresa \"" + empresa.getNombre() + "\" fue aprobada. Ya podés gestionarla desde Mis empresas."
                : "Tu empresa \"" + empresa.getNombre() + "\" fue rechazada."
                    + (motivo != null && !motivo.isBlank() ? " Motivo: " + motivo : "");

        Notificacion notif = notificacionRepository.save(Notificacion.builder()
                .empresa(empresa)
                .creadoPor(duenio)
                .titulo(titulo)
                .mensaje(mensaje)
                .categoria(CategoriaNotificacion.GENERAL)
                .prioridad(PrioridadNotificacion.ALTA)
                .origen(OrigenNotificacion.SISTEMA)
                .build());
        crearDestinatario(notif, duenio);
    }

    // ── Bandeja del usuario ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificacionResponse> misNotificaciones(String emailUsuario) {
        Usuario u = usuario(emailUsuario);
        return destinatarioRepository
                .findByUsuarioIdOrderByLeidaAscNotificacion_FechaCreacionDesc(u.getId())
                .stream().map(NotificacionResponse::fromEntity).toList();
    }

    /** Notificaciones que el usuario envió, con estadística de lectura. */
    @Transactional(readOnly = true)
    public List<NotificacionEnviadaResponse> notificacionesEnviadas(String emailUsuario) {
        Usuario u = usuario(emailUsuario);
        return notificacionRepository
                .findByCreadoPor_IdAndOrigenNotOrderByFechaCreacionDesc(u.getId(), OrigenNotificacion.SISTEMA)
                .stream().map(NotificacionEnviadaResponse::fromEntity).toList();
    }

    /** Borra una notificación que el usuario envió (elimina la copia de todos los destinatarios). */
    @Transactional
    public void eliminarEnviada(Long notificacionId, String emailUsuario) {
        Usuario u = usuario(emailUsuario);
        Notificacion n = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", notificacionId));
        if (n.getCreadoPor() == null || !n.getCreadoPor().getId().equals(u.getId())) {
            throw new ResourceNotFoundException("Notificación", notificacionId);
        }
        notificacionRepository.delete(n);
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

    /** Elimina la notificación de la bandeja del usuario (borra su copia). */
    @Transactional
    public void eliminarDeBandeja(Long destinatarioId, String emailUsuario) {
        Usuario u = usuario(emailUsuario);
        NotificacionDestinatario d = destinatarioRepository
                .findByIdAndUsuarioId(destinatarioId, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", destinatarioId));
        destinatarioRepository.delete(d);
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
