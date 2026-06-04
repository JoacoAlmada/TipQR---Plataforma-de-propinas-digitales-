package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.LoginRequest;
import tipqr.back.dto.LoginResponse;
import tipqr.back.dto.RegistroRequest;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.entity.enums.Rol;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.repository.EmpresaRepository;
import tipqr.back.repository.UsuarioRepository;
import tipqr.back.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (usuario.getRol() != Rol.SUPERADMIN
                && usuario.getEstadoCuenta() != EstadoCuenta.APROBADA) {
            throw new DisabledException(mensajePorEstado(usuario.getEstadoCuenta()));
        }

        UserDetails userDetails = usuarioService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(
                token,
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getNombre(),
                usuario.getApellido());
    }

    private String mensajePorEstado(EstadoCuenta estado) {
        return switch (estado) {
            case PENDIENTE_VALIDACION ->
                    "Tu cuenta está pendiente de validación. Te avisaremos por email (suele demorar de 1 a 4 horas).";
            case RECHAZADA ->
                    "Tu solicitud fue rechazada. Escribinos para más información.";
            default ->
                    "Tu registro no está completo. Terminá el alta para poder ingresar.";
        };
    }

    /**
     * Alta self-service: crea la empresa y su usuario dueño en un solo paso,
     * y devuelve el token para dejar al usuario logueado.
     */
    @Transactional
    public LoginResponse registrar(RegistroRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + email);
        }

        String cuit = (request.getCuit() == null || request.getCuit().isBlank())
                ? null : request.getCuit().trim();
        if (cuit != null && empresaRepository.existsByCuit(cuit)) {
            throw new DuplicateResourceException("Ya existe una empresa con el CUIT " + cuit);
        }

        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre(request.getNombreEmpresa().trim())
                .rubro(request.getRubro())
                .cuit(cuit)
                .emailContacto(email)
                .estado(true)
                .build());

        Usuario dueno = usuarioRepository.save(Usuario.builder()
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.DUENO)
                .estadoCuenta(EstadoCuenta.APROBADA)
                .emailVerificado(true)
                .empresa(empresa)
                .estado(true)
                .build());

        UserDetails userDetails = usuarioService.loadUserByUsername(dueno.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(
                token,
                dueno.getEmail(),
                dueno.getRol().name(),
                dueno.getNombre(),
                dueno.getApellido());
    }
}
