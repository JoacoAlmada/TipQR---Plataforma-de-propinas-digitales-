package tipqr.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.Rol;
import tipqr.back.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByEmail("admin@tipqr.com")) {
            log.info("Usuario admin ya existe — omitiendo inicialización");
            return;
        }

        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa Demo");
        empresa.setRubro("Gastronomía");
        empresa.setCuit("30-12345678-9");
        empresa.setEmailContacto("demo@tipqr.com");
        empresa.setTelefono("3513000000");
        em.persist(empresa);

        Usuario admin = Usuario.builder()
                .nombre("Admin")
                .apellido("TipQR")
                .email("admin@tipqr.com")
                .password(passwordEncoder.encode("tipqr2026"))
                .rol(Rol.DUENO)
                .empresa(empresa)
                .build();
        usuarioRepository.save(admin);

        log.info("Usuario admin creado: admin@tipqr.com / tipqr2026");
    }
}
