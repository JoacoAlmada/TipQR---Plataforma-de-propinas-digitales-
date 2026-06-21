package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.EmpleadoRequest;
import tipqr.back.dto.EmpleadoResponse;
import tipqr.back.dto.SucursalResponse;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.entity.enums.Rol;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpleadoRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpleadoService {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private final SecureRandom random = new SecureRandom();

    private final EmpleadoRepository empleadoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final QrService qrService;

    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listar(String emailUsuario, Long sucursalId) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        List<Empleado> empleados;
        if (sucursalId != null) {
            // Valida que la sucursal sea de la empresa del usuario.
            sucursalRepository.findByIdAndEmpresaId(sucursalId, empresa.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal", sucursalId));
            empleados = empleadoRepository.findBySucursalIdOrderByNombreVisibleAsc(sucursalId);
        } else {
            empleados = empleadoRepository.findBySucursal_Empresa_IdOrderByNombreVisibleAsc(empresa.getId());
        }
        return empleados.stream().map(EmpleadoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public EmpleadoResponse obtenerPorId(Long id, String emailUsuario) {
        return EmpleadoResponse.fromEntity(empleadoPropio(id, emailUsuario));
    }

    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + email);
        }
        Sucursal sucursal = sucursalRepository
                .findByIdAndEmpresaId(request.getSucursalId(), empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", request.getSucursalId()));

        String passwordTemporal = generarPassword();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre(request.getNombreVisible().trim())
                .apellido(request.getApellido().trim())
                .email(email)
                .password(passwordEncoder.encode(passwordTemporal))
                .rol(Rol.EMPLEADO)
                .estadoCuenta(EstadoCuenta.APROBADA)
                .emailVerificado(true)
                .empresa(empresa)
                .estado(true)
                .build());

        Empleado empleado = empleadoRepository.save(Empleado.builder()
                .usuario(usuario)
                .sucursal(sucursal)
                .nombreVisible(request.getNombreVisible().trim())
                .puesto(request.getPuesto())
                .estado(true)
                .build());

        // Alta automática del código QR del empleado.
        qrService.generarParaEmpleado(empleado);

        // Best-effort: si falla el mail, no se cae la creación.
        try {
            emailService.enviarBienvenidaEmpleado(email, usuario.getNombre(), passwordTemporal);
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de bienvenida al empleado {}: {}", email, e.getMessage());
        }

        EmpleadoResponse response = EmpleadoResponse.fromEntity(empleado);
        response.setPasswordTemporal(passwordTemporal);
        return response;
    }

    @Transactional
    public EmpleadoResponse actualizar(Long id, EmpleadoRequest request, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        Empleado empleado = empleadoPropio(id, emailUsuario);
        Sucursal sucursal = sucursalRepository
                .findByIdAndEmpresaId(request.getSucursalId(), empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", request.getSucursalId()));

        Usuario usuario = empleado.getUsuario();
        String nuevoEmail = request.getEmail().trim().toLowerCase();
        if (!nuevoEmail.equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(nuevoEmail)) {
                throw new DuplicateResourceException("Ya existe un usuario con el email " + nuevoEmail);
            }
            usuario.setEmail(nuevoEmail);
        }
        usuario.setNombre(request.getNombreVisible().trim());
        usuario.setApellido(request.getApellido().trim());
        usuarioRepository.save(usuario);

        empleado.setNombreVisible(request.getNombreVisible().trim());
        empleado.setPuesto(request.getPuesto());
        empleado.setSucursal(sucursal);
        return EmpleadoResponse.fromEntity(empleadoRepository.save(empleado));
    }

    @Transactional
    public EmpleadoResponse cambiarEstado(Long id, boolean estado, String emailUsuario) {
        Empleado empleado = empleadoPropio(id, emailUsuario);
        empleado.setEstado(estado);
        // Un empleado inactivo no debe poder ingresar.
        empleado.getUsuario().setEstado(estado);
        usuarioRepository.save(empleado.getUsuario());
        return EmpleadoResponse.fromEntity(empleadoRepository.save(empleado));
    }

    /**
     * Marca (o desmarca) a un empleado como encargado de su sucursal cambiando
     * el rol de su usuario entre ENCARGADO y EMPLEADO.
     */
    @Transactional
    public EmpleadoResponse marcarEncargado(Long id, boolean valor, String emailUsuario) {
        Empleado empleado = empleadoPropio(id, emailUsuario);
        Usuario usuario = empleado.getUsuario();
        usuario.setRol(valor ? Rol.ENCARGADO : Rol.EMPLEADO);
        usuarioRepository.save(usuario);
        return EmpleadoResponse.fromEntity(empleado);
    }

    /**
     * Sucursal del usuario logueado (su registro de empleado). Para el panel del encargado.
     */
    @Transactional(readOnly = true)
    public SucursalResponse miSucursal(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Empleado empleado = usuario.getEmpleado();
        if (empleado == null || empleado.getSucursal() == null) {
            throw new ResourceNotFoundException("El usuario no tiene una sucursal asignada");
        }
        return SucursalResponse.fromEntity(empleado.getSucursal());
    }

    // ── Helpers ─────────────────────────────────────────

    private Empresa empresaDelUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }

    private Empleado empleadoPropio(Long id, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        return empleadoRepository.findByIdAndSucursal_Empresa_Id(id, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", id));
    }

    private String generarPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
