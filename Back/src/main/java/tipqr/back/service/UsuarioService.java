package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tipqr.back.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> User.builder()
                        .username(usuario.getEmail())
                        .password(usuario.getPassword())
                        .roles(usuario.getRol().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
