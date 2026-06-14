package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Empleado;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    boolean existsBySucursalIdAndEstadoTrue(Long sucursalId);

    /** Todos los empleados de la empresa (navega sucursal -> empresa). */
    List<Empleado> findBySucursal_Empresa_IdOrderByNombreVisibleAsc(Long empresaId);

    /** Empleados de una sucursal puntual. */
    List<Empleado> findBySucursalIdOrderByNombreVisibleAsc(Long sucursalId);

    /** Empleado por id, acotado a la empresa del usuario. */
    Optional<Empleado> findByIdAndSucursal_Empresa_Id(Long id, Long empresaId);
}
