package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tipqr.back.dto.LoginRequest;
import tipqr.back.dto.LoginResponse;
import tipqr.back.entity.Usuario;
import tipqr.back.repository.UsuarioRepository;
import tipqr.back.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
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
}
