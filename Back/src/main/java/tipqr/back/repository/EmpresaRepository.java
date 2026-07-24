package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.enums.EstadoValidacionEmpresa;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByCuit(String cuit);

    boolean existsByCuitAndIdNot(String cuit, Long id);

    List<Empresa> findByPropietarioIdOrderByNombreAsc(Long propietarioId);

    /** Empresas en un estado de validación (para el panel del superadmin). */
    List<Empresa> findByEstadoValidacionOrderByFechaCreacionDesc(EstadoValidacionEmpresa estado);

    /** Empresas previas a la validación (estado nulo): se consideran aprobadas. */
    List<Empresa> findByEstadoValidacionIsNullOrderByFechaCreacionDesc();
}
