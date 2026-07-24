package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tipqr.back.dto.EmpresaRequest;
import tipqr.back.dto.EmpresaResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.EstadoValidacionEmpresa;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpresaRepository;
import tipqr.back.repository.UsuarioRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private static final String TIPO_PDF = "application/pdf";

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Empresa del usuario autenticado. Listado acotado (un solo comercio por usuario).
     */
    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar(String emailUsuario) {
        return List.of(EmpresaResponse.fromEntity(empresaDelUsuario(emailUsuario)));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse miEmpresa(String emailUsuario) {
        return EmpresaResponse.fromEntity(empresaDelUsuario(emailUsuario));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPorId(Long id, String emailUsuario) {
        return EmpresaResponse.fromEntity(empresaPropia(id, emailUsuario));
    }

    @Transactional
    public EmpresaResponse actualizar(Long id, EmpresaRequest request, String emailUsuario) {
        // empresaEnPropiedad permite editar cualquier empresa propia (no solo la activa),
        // necesario para corregir una empresa rechazada antes de reenviarla.
        Empresa empresa = empresaEnPropiedad(id, emailUsuario);
        validarCuitUnico(request.getCuit(), empresa.getId());

        empresa.setNombre(request.getNombre());
        empresa.setNombreFantasia(request.getNombreFantasia());
        empresa.setRubro(request.getRubro());
        empresa.setCuit(normalizar(request.getCuit()));
        empresa.setProvincia(request.getProvincia());
        empresa.setCalle(request.getCalle());
        empresa.setNumeracion(request.getNumeracion());
        empresa.setEmailContacto(request.getEmailContacto());
        empresa.setTelefono(request.getTelefono());

        return EmpresaResponse.fromEntity(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponse cambiarEstado(Long id, boolean estado, String emailUsuario) {
        Empresa empresa = empresaPropia(id, emailUsuario);
        empresa.setEstado(estado);
        return EmpresaResponse.fromEntity(empresaRepository.save(empresa));
    }

    // ── Multi-empresa (un dueño puede administrar varias) ──

    /** Empresas que administra el dueño, marcando cuál es la activa (la que está gestionando). */
    @Transactional
    public List<EmpresaResponse> misEmpresas(String emailUsuario) {
        Usuario usuario = usuario(emailUsuario);
        Empresa activa = usuario.getEmpresa();
        // Backfill: la empresa que ya gestiona pasa a tener propietario si no lo tenía.
        if (activa != null && activa.getPropietario() == null) {
            activa.setPropietario(usuario);
            empresaRepository.save(activa);
        }

        List<Empresa> propias = new ArrayList<>(
                empresaRepository.findByPropietarioIdOrderByNombreAsc(usuario.getId()));
        if (activa != null && propias.stream().noneMatch(e -> e.getId().equals(activa.getId()))) {
            propias.add(activa);
        }

        Long activaId = activa != null ? activa.getId() : null;
        return propias.stream()
                .map(e -> EmpresaResponse.fromEntity(e, e.getId().equals(activaId)))
                .toList();
    }

    /**
     * Da de alta una empresa adicional para el dueño. Nace PENDIENTE de validación: NO se activa
     * ni se puede gestionar hasta que el superadmin la apruebe. El alta se completa subiendo la
     * constancia de AFIP ({@link #subirConstancia}).
     */
    @Transactional
    public EmpresaResponse crear(EmpresaRequest request, String emailUsuario) {
        Usuario usuario = usuario(emailUsuario);
        String cuit = normalizar(request.getCuit());
        if (cuit != null && empresaRepository.existsByCuit(cuit)) {
            throw new DuplicateResourceException("Ya existe una empresa con el CUIT " + cuit);
        }

        Empresa empresa = Empresa.builder()
                .nombre(request.getNombre().trim())
                .nombreFantasia(request.getNombreFantasia())
                .rubro(request.getRubro())
                .cuit(cuit)
                .provincia(request.getProvincia())
                .calle(request.getCalle())
                .numeracion(request.getNumeracion())
                .emailContacto(request.getEmailContacto())
                .telefono(request.getTelefono())
                .estado(true)
                .estadoValidacion(EstadoValidacionEmpresa.PENDIENTE)
                .propietario(usuario)
                .build();
        empresa = empresaRepository.save(empresa);
        // No se toca la empresa activa: la nueva queda pendiente de validación.
        return EmpresaResponse.fromEntity(empresa, false);
    }

    /** Sube o reemplaza la constancia de AFIP de una empresa propia (mientras esté pendiente). */
    @Transactional
    public EmpresaResponse subirConstancia(Long id, String emailUsuario, MultipartFile archivo) {
        Empresa empresa = empresaEnPropiedad(id, emailUsuario);
        if (empresa.getEstadoValidacion() == EstadoValidacionEmpresa.APROBADA) {
            throw new IllegalArgumentException("La empresa ya está validada.");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }
        if (!TIPO_PDF.equals(archivo.getContentType())) {
            throw new IllegalArgumentException("La constancia de AFIP debe ser un PDF.");
        }
        try {
            empresa.setConstanciaDatos(archivo.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo.");
        }
        empresa.setConstanciaNombre(archivo.getOriginalFilename());
        empresa.setConstanciaContentType(archivo.getContentType());
        return EmpresaResponse.fromEntity(empresaRepository.save(empresa), false);
    }

    /**
     * Reenvía a validación una empresa rechazada (tras corregir datos o constancia): vuelve a
     * PENDIENTE y limpia el motivo. Requiere la constancia cargada.
     */
    @Transactional
    public EmpresaResponse reenviarValidacion(Long id, String emailUsuario) {
        Empresa empresa = empresaEnPropiedad(id, emailUsuario);
        if (empresa.getEstadoValidacion() != EstadoValidacionEmpresa.RECHAZADA) {
            throw new IllegalArgumentException("Solo se puede reenviar una empresa que fue rechazada.");
        }
        if (empresa.getConstanciaNombre() == null) {
            throw new IllegalArgumentException("Subí la constancia de AFIP antes de reenviar.");
        }
        empresa.setEstadoValidacion(EstadoValidacionEmpresa.PENDIENTE);
        empresa.setMotivoRechazo(null);
        return EmpresaResponse.fromEntity(empresaRepository.save(empresa), false);
    }

    /** Cambia la empresa que el dueño está gestionando (debe ser propia y estar aprobada). */
    @Transactional
    public EmpresaResponse cambiarActiva(Long id, String emailUsuario) {
        Usuario usuario = usuario(emailUsuario);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));

        boolean propia = empresa.getPropietario() != null
                && empresa.getPropietario().getId().equals(usuario.getId());
        boolean esLaActual = usuario.getEmpresa() != null
                && usuario.getEmpresa().getId().equals(id);
        if (!propia && !esLaActual) {
            throw new ResourceNotFoundException("Empresa", id); // no revela empresas ajenas
        }
        // null = empresas previas a esta feature (se consideran aprobadas).
        if (empresa.getEstadoValidacion() != null
                && empresa.getEstadoValidacion() != EstadoValidacionEmpresa.APROBADA) {
            throw new IllegalArgumentException("La empresa está pendiente de validación: todavía no se puede gestionar.");
        }

        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);
        return EmpresaResponse.fromEntity(empresa, true);
    }

    /**
     * Empresa asociada al usuario autenticado (resuelta desde el contexto, no del request).
     */
    private Usuario usuario(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Empresa empresaDelUsuario(String emailUsuario) {
        Empresa empresa = usuario(emailUsuario).getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }

    /**
     * Valida que la empresa solicitada por id sea la del usuario. Si no, 404 (no se
     * revela la existencia de empresas ajenas).
     */
    private Empresa empresaPropia(Long id, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        if (!empresa.getId().equals(id)) {
            throw new ResourceNotFoundException("Empresa", id);
        }
        return empresa;
    }

    /**
     * Empresa cuyo propietario es el usuario autenticado (o su empresa activa). 404 si no.
     * A diferencia de {@link #empresaPropia}, permite operar empresas propias que no son la activa
     * (por ejemplo una recién dada de alta y pendiente de validación).
     */
    private Empresa empresaEnPropiedad(Long id, String emailUsuario) {
        Usuario usuario = usuario(emailUsuario);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
        boolean propia = empresa.getPropietario() != null
                && empresa.getPropietario().getId().equals(usuario.getId());
        boolean esLaActual = usuario.getEmpresa() != null
                && usuario.getEmpresa().getId().equals(id);
        if (!propia && !esLaActual) {
            throw new ResourceNotFoundException("Empresa", id);
        }
        return empresa;
    }

    private void validarCuitUnico(String cuit, Long idActual) {
        String cuitNormalizado = normalizar(cuit);
        if (cuitNormalizado == null) {
            return;
        }
        if (empresaRepository.existsByCuitAndIdNot(cuitNormalizado, idActual)) {
            throw new DuplicateResourceException("Ya existe una empresa con el CUIT " + cuitNormalizado);
        }
    }

    private String normalizar(String cuit) {
        if (cuit == null || cuit.isBlank()) {
            return null;
        }
        return cuit.trim();
    }
}
