package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.GrupoPropinaEmpleado;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoPropinaEmpleadoRepository extends JpaRepository<GrupoPropinaEmpleado, Long> {

    List<GrupoPropinaEmpleado> findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(Long grupoPropinaId);

    boolean existsByGrupoPropinaIdAndEmpleadoId(Long grupoPropinaId, Long empleadoId);

    Optional<GrupoPropinaEmpleado> findByGrupoPropinaIdAndEmpleadoId(Long grupoPropinaId, Long empleadoId);
}
