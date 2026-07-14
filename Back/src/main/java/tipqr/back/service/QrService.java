package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.MesaDestinatariosResponse;
import tipqr.back.dto.QrDestinoResponse;
import tipqr.back.dto.QrResponse;
import tipqr.back.entity.CodigoQR;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.Mesa;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.TipoDestinoQR;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.CodigoQRRepository;
import tipqr.back.repository.GrupoPropinaEmpleadoRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

/**
 * Códigos QR de propina. Se genera uno (único) por mesa y por empleado, de forma automática
 * al darlos de alta. Cada QR apunta a la pantalla pública con su código.
 */
@Service
@RequiredArgsConstructor
public class QrService {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LARGO_CODIGO = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CodigoQRRepository qrRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final QrGenerator qrGenerator;
    private final GrupoPropinaEmpleadoRepository miembroRepository;
    private final TurnoService turnoService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    // ── Alta automática (llamada desde MesaService / EmpleadoService) ─────────

    /** Crea el QR de una mesa si todavía no tiene (idempotente). */
    @Transactional
    public CodigoQR generarParaMesa(Mesa mesa) {
        return qrRepository.findByMesaId(mesa.getId()).orElseGet(() -> {
            String codigo = generarCodigoUnico();
            return qrRepository.save(CodigoQR.builder()
                    .codigo(codigo)
                    .tipoDestino(TipoDestinoQR.MESA)
                    .sucursal(mesa.getSucursal())
                    .mesa(mesa)
                    .url(construirUrl(codigo))
                    .activo(true)
                    .build());
        });
    }

    /** Crea el QR de un empleado si todavía no tiene (idempotente). */
    @Transactional
    public CodigoQR generarParaEmpleado(Empleado empleado) {
        return qrRepository.findByEmpleadoId(empleado.getId()).orElseGet(() -> {
            String codigo = generarCodigoUnico();
            return qrRepository.save(CodigoQR.builder()
                    .codigo(codigo)
                    .tipoDestino(TipoDestinoQR.EMPLEADO)
                    .sucursal(empleado.getSucursal())
                    .empleado(empleado)
                    .url(construirUrl(codigo))
                    .activo(true)
                    .build());
        });
    }

    // ── Resolución pública (pantalla del cliente) ─────────────────────────────

    /** Resuelve un QR activo por su código para mostrar el destino en la pantalla pública. */
    @Transactional(readOnly = true)
    public QrDestinoResponse resolverPublico(String codigo) {
        CodigoQR qr = qrRepository.findByCodigo(codigo)
                .filter(q -> Boolean.TRUE.equals(q.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Código QR " + codigo + " no encontrado o inactivo"));
        return QrDestinoResponse.fromEntity(qr);
    }

    /**
     * Destinatarios de la propina de una mesa: los mozos del turno activo (para elegir individual)
     * o el equipo completo. Si no hay turno activo, la lista viene vacía y turnoActivo = false.
     */
    @Transactional(readOnly = true)
    public MesaDestinatariosResponse resolverDestinatariosMesa(String codigo) {
        CodigoQR qr = qrRepository.findByCodigo(codigo)
                .filter(q -> Boolean.TRUE.equals(q.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Código QR " + codigo + " no encontrado o inactivo"));

        Optional<GrupoPropina> grupoActivo = turnoService.grupoActivo(qr.getSucursal().getId());
        List<MesaDestinatariosResponse.Mozo> mozos = grupoActivo
                .map(g -> miembroRepository.findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(g.getId())
                        .stream()
                        .map(m -> new MesaDestinatariosResponse.Mozo(
                                m.getEmpleado().getId(), m.getEmpleado().getNombreVisible()))
                        .toList())
                .orElse(List.of());

        String destinoNombre = qr.getMesa() != null ? "Mesa " + qr.getMesa().getNumero() : null;
        String empresaNombre = (qr.getSucursal() != null && qr.getSucursal().getEmpresa() != null)
                ? qr.getSucursal().getEmpresa().getNombre() : null;

        return MesaDestinatariosResponse.builder()
                .codigo(qr.getCodigo())
                .destinoNombre(destinoNombre)
                .sucursalNombre(qr.getSucursal() != null ? qr.getSucursal().getNombre() : null)
                .empresaNombre(empresaNombre)
                .turnoActivo(grupoActivo.isPresent())
                .grupoNombre(grupoActivo.map(GrupoPropina::getNombre).orElse(null))
                .mozos(mozos)
                .build();
    }

    // ── Mi QR (empleado) ──────────────────────────────────────────────────────

    /** QR del empleado logueado (lo genera si todavía no tiene). */
    @Transactional
    public QrResponse miQr(String emailUsuario) {
        return QrResponse.fromEntity(generarParaEmpleado(empleadoDelUsuario(emailUsuario)));
    }

    /** Imagen PNG del QR del empleado logueado. */
    @Transactional
    public byte[] miQrImagen(String emailUsuario) {
        CodigoQR qr = generarParaEmpleado(empleadoDelUsuario(emailUsuario));
        return qrGenerator.generarPng(qr.getUrl());
    }

    private Empleado empleadoDelUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Empleado empleado = usuario.getEmpleado();
        if (empleado == null) {
            throw new ResourceNotFoundException("El usuario no tiene un empleado asociado");
        }
        return empleado;
    }

    // ── Consulta (panel de administración) ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<QrResponse> listar(String emailUsuario, Long sucursalId) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        List<CodigoQR> codigos;
        if (sucursalId != null) {
            sucursalRepository.findByIdAndEmpresaId(sucursalId, empresa.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal", sucursalId));
            codigos = qrRepository.findBySucursalIdOrderByFechaCreacionDesc(sucursalId);
        } else {
            codigos = qrRepository.findBySucursal_Empresa_IdOrderByFechaCreacionDesc(empresa.getId());
        }
        return codigos.stream().map(QrResponse::fromEntity).toList();
    }

    /** Imagen PNG del QR (para previsualizar/descargar en el panel). */
    @Transactional(readOnly = true)
    public byte[] obtenerImagenPng(Long id, String emailUsuario) {
        CodigoQR qr = qrPropio(id, emailUsuario);
        return qrGenerator.generarPng(qr.getUrl());
    }

    /** Regenera el código (y la URL) de un QR, invalidando el anterior. */
    @Transactional
    public QrResponse regenerar(Long id, String emailUsuario) {
        CodigoQR qr = qrPropio(id, emailUsuario);
        String codigo = generarCodigoUnico();
        qr.setCodigo(codigo);
        qr.setUrl(construirUrl(codigo));
        qr.setActivo(true);
        return QrResponse.fromEntity(qrRepository.save(qr));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String construirUrl(String codigo) {
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base + "/propina/" + codigo;
    }

    private Empresa empresaDelUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }

    private CodigoQR qrPropio(Long id, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        return qrRepository.findByIdAndSucursal_Empresa_Id(id, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Código QR", id));
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder(LARGO_CODIGO);
            for (int i = 0; i < LARGO_CODIGO; i++) {
                sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
            }
            codigo = sb.toString();
        } while (qrRepository.existsByCodigo(codigo));
        return codigo;
    }
}
