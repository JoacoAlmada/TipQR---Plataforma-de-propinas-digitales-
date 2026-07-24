package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tipqr.back.dto.RegistroDatosRequest;
import tipqr.back.dto.RegistroEstadoResponse;
import tipqr.back.dto.RegistroPaso1Request;
import tipqr.back.dto.RegistroPaso2Request;
import tipqr.back.dto.RegistroResumenResponse;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.entity.enums.EstadoValidacionEmpresa;
import tipqr.back.entity.enums.Rol;
import tipqr.back.entity.enums.TipoDocumento;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.DocumentoRegistroRepository;
import tipqr.back.repository.EmpresaRepository;
import tipqr.back.repository.UsuarioRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private static final Set<String> TIPOS_IMAGEN = Set.of("image/jpeg", "image/jpg", "image/png");
    private static final String TIPO_PDF = "application/pdf";

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final DocumentoRegistroRepository documentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RecaptchaService recaptchaService;

    /**
     * Paso 1: valida captcha, crea la cuenta en estado CREADA y envía el email
     * de verificación. Devuelve el token de registro para continuar el wizard.
     */
    @Transactional
    public String paso1(RegistroPaso1Request request) {
        if (!recaptchaService.esValido(request.getCaptchaToken())) {
            throw new IllegalArgumentException("Verificación de captcha fallida. Volvé a intentar.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe una cuenta con el email " + email);
        }

        String cuit = request.getCuit().trim();
        if (usuarioRepository.existsByCuit(cuit)) {
            throw new DuplicateResourceException("Ya existe una cuenta con el CUIT " + cuit);
        }

        String dni = request.getDni().trim();
        if (usuarioRepository.existsByDni(dni)) {
            throw new DuplicateResourceException("Ya existe una cuenta con el DNI " + dni);
        }

        String token = UUID.randomUUID().toString();

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono().trim())
                .cuit(cuit)
                .dni(dni)
                .rol(Rol.DUENO)
                .estadoCuenta(EstadoCuenta.CREADA)
                .emailVerificado(false)
                .emailToken(token)
                .estado(true)
                .build();
        usuarioRepository.save(usuario);

        emailService.enviarVerificacion(email, usuario.getNombre(), token);
        return token;
    }

    /**
     * Verifica el email a partir del token del link. La cuenta pasa a VERIFICADA.
     */
    @Transactional
    public void verificarEmail(String token) {
        Usuario usuario = usuarioRepository.findByEmailToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token de verificación inválido"));

        if (!Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            usuario.setEmailVerificado(true);
            if (usuario.getEstadoCuenta() == EstadoCuenta.CREADA) {
                usuario.setEstadoCuenta(EstadoCuenta.VERIFICADA);
            }
            usuarioRepository.save(usuario);
        }
    }

    /**
     * Estado del registro en curso (para que el wizard sepa si avanzar).
     */
    @Transactional(readOnly = true)
    public RegistroEstadoResponse estado(String token) {
        Usuario usuario = registroPorToken(token);
        return new RegistroEstadoResponse(
                token,
                usuario.getEstadoCuenta().name(),
                Boolean.TRUE.equals(usuario.getEmailVerificado()));
    }

    /**
     * Datos completos del registro para retomarlo (corregir y reenviar tras un rechazo).
     */
    @Transactional(readOnly = true)
    public RegistroResumenResponse resumen(String token) {
        Usuario u = registroPorToken(token);
        Empresa e = u.getEmpresa();
        List<String> docs = documentoRepository.findByUsuarioId(u.getId())
                .stream().map(d -> d.getTipo().name()).toList();
        return RegistroResumenResponse.builder()
                .registroToken(token)
                .estadoCuenta(u.getEstadoCuenta().name())
                .motivoRechazo(u.getMotivoRechazo())
                .nombre(u.getNombre()).apellido(u.getApellido()).email(u.getEmail())
                .telefono(u.getTelefono()).cuit(u.getCuit()).dni(u.getDni())
                .nombreEmpresa(e != null ? e.getNombre() : null)
                .nombreFantasia(e != null ? e.getNombreFantasia() : null)
                .provincia(e != null ? e.getProvincia() : null)
                .calle(e != null ? e.getCalle() : null)
                .numeracion(e != null ? e.getNumeracion() : null)
                .empresaCuit(e != null ? e.getCuit() : null)
                .rubro(e != null ? e.getRubro() : null)
                .documentosCargados(docs)
                .build();
    }

    /**
     * Corrige los datos personales del dueño al retomar el registro (email no se cambia).
     */
    @Transactional
    public void actualizarDatosPersonales(String token, RegistroDatosRequest req) {
        Usuario usuario = registroPorToken(token);
        exigirEmailVerificado(usuario);

        String cuit = req.getCuit().trim();
        if (!cuit.equals(usuario.getCuit()) && usuarioRepository.existsByCuit(cuit)) {
            throw new DuplicateResourceException("Ya existe una cuenta con el CUIT " + cuit);
        }
        String dni = req.getDni().trim();
        if (!dni.equals(usuario.getDni()) && usuarioRepository.existsByDni(dni)) {
            throw new DuplicateResourceException("Ya existe una cuenta con el DNI " + dni);
        }

        usuario.setNombre(req.getNombre().trim());
        usuario.setApellido(req.getApellido().trim());
        usuario.setTelefono(req.getTelefono().trim());
        usuario.setCuit(cuit);
        usuario.setDni(dni);
        usuarioRepository.save(usuario);
    }

    /**
     * Paso 2: datos del comercio. Requiere el email ya verificado.
     */
    @Transactional
    public void paso2(String token, RegistroPaso2Request request) {
        Usuario usuario = registroPorToken(token);
        exigirEmailVerificado(usuario);

        String cuit = (request.getCuit() == null || request.getCuit().isBlank())
                ? null : request.getCuit().trim();
        if (cuit != null) {
            boolean duplicado = usuario.getEmpresa() == null
                    ? empresaRepository.existsByCuit(cuit)
                    : empresaRepository.existsByCuitAndIdNot(cuit, usuario.getEmpresa().getId());
            if (duplicado) {
                throw new DuplicateResourceException("Ya existe una empresa con el CUIT " + cuit);
            }
        }

        Empresa empresa = usuario.getEmpresa() != null ? usuario.getEmpresa() : new Empresa();
        empresa.setNombre(request.getNombreEmpresa().trim());
        empresa.setNombreFantasia(request.getNombreFantasia());
        empresa.setProvincia(request.getProvincia());
        empresa.setCalle(request.getCalle());
        empresa.setNumeracion(request.getNumeracion());
        empresa.setCuit(cuit);
        empresa.setRubro(request.getRubro());
        empresa.setEmailContacto(usuario.getEmail());
        empresa.setTelefono(usuario.getTelefono());
        empresa.setEstado(true);
        // La empresa del registro se valida junto con la cuenta del dueño (no requiere revisión aparte).
        empresa.setEstadoValidacion(EstadoValidacionEmpresa.APROBADA);
        empresa = empresaRepository.save(empresa);

        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);
    }

    /**
     * Paso 3: sube (o reemplaza) un documento. Valida el tipo de archivo.
     */
    @Transactional
    public void subirDocumento(String token, TipoDocumento tipo, MultipartFile archivo) {
        Usuario usuario = registroPorToken(token);
        exigirEmailVerificado(usuario);

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        validarTipoArchivo(tipo, archivo.getContentType());

        DocumentoRegistro doc = documentoRepository
                .findByUsuarioIdAndTipo(usuario.getId(), tipo)
                .orElseGet(DocumentoRegistro::new);
        doc.setUsuario(usuario);
        doc.setTipo(tipo);
        doc.setNombreArchivo(archivo.getOriginalFilename());
        doc.setContentType(archivo.getContentType());
        try {
            doc.setDatos(archivo.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo");
        }
        documentoRepository.save(doc);
    }

    /**
     * Tipos de documento ya cargados (para habilitar el botón finalizar).
     */
    @Transactional(readOnly = true)
    public List<TipoDocumento> documentosCargados(String token) {
        Usuario usuario = registroPorToken(token);
        return documentoRepository.findByUsuarioId(usuario.getId())
                .stream().map(DocumentoRegistro::getTipo).toList();
    }

    /**
     * Finaliza el registro: requiere comercio + los 4 documentos. La cuenta
     * pasa a PENDIENTE_VALIDACION (la revisa el superadmin).
     */
    @Transactional
    public void finalizar(String token) {
        Usuario usuario = registroPorToken(token);
        exigirEmailVerificado(usuario);

        if (usuario.getEmpresa() == null) {
            throw new IllegalArgumentException("Faltan completar los datos del comercio (paso 2)");
        }
        List<TipoDocumento> cargados = documentoRepository.findByUsuarioId(usuario.getId())
                .stream().map(DocumentoRegistro::getTipo).toList();
        for (TipoDocumento requerido : TipoDocumento.values()) {
            if (!cargados.contains(requerido)) {
                throw new IllegalArgumentException("Falta adjuntar: " + requerido.name());
            }
        }

        usuario.setEstadoCuenta(EstadoCuenta.PENDIENTE_VALIDACION);
        usuario.setMotivoRechazo(null); // si se reenvía tras un rechazo, se limpia el motivo
        usuarioRepository.save(usuario);
    }

    // ── Helpers ─────────────────────────────────────────

    private Usuario registroPorToken(String token) {
        return usuarioRepository.findByEmailToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
    }

    private void exigirEmailVerificado(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new IllegalArgumentException("Primero verificá tu email");
        }
    }

    private void validarTipoArchivo(TipoDocumento tipo, String contentType) {
        if (tipo == TipoDocumento.CONSTANCIA_AFIP) {
            if (!TIPO_PDF.equals(contentType)) {
                throw new IllegalArgumentException("La constancia de AFIP debe ser un PDF");
            }
        } else if (contentType == null || !TIPOS_IMAGEN.contains(contentType)) {
            throw new IllegalArgumentException("El documento " + tipo.name() + " debe ser una imagen (JPG o PNG)");
        }
    }
}
