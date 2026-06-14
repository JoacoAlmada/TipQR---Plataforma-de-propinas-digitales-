package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Sucursal;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByEmpresaIdOrderByNombreAsc(Long empresaId);

    Optional<Sucursal> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByNombreIgnoreCaseAndEmpresaId(String nombre, Long empresaId);

    boolean existsByNombreIgnoreCaseAndEmpresaIdAndIdNot(String nombre, Long empresaId, Long id);
}
