package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.entity.enums.Rol;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCuit(String cuit);

    boolean existsByDni(String dni);

    Optional<Usuario> findByEmailToken(String emailToken);

    List<Usuario> findByEstadoCuentaOrderByFechaCreacionDesc(EstadoCuenta estadoCuenta);

    // Emisor de los avisos automáticos: el dueño de la empresa.
    Optional<Usuario> findFirstByEmpresa_IdAndRolOrderByIdAsc(Long empresaId, Rol rol);
}
