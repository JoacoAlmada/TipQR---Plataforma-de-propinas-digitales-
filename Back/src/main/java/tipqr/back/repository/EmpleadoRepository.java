package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.enums.Rol;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    boolean existsBySucursalIdAndEstadoTrue(Long sucursalId);

    /** Todos los empleados de la empresa (navega sucursal -> empresa). */
    List<Empleado> findBySucursal_Empresa_IdOrderByNombreVisibleAsc(Long empresaId);

    /** Empleados de una sucursal puntual. */
    List<Empleado> findBySucursalIdOrderByNombreVisibleAsc(Long sucursalId);

    /** Empleados de la empresa cuyo usuario tiene un rol dado (para notificar por rol). */
    List<Empleado> findBySucursal_Empresa_IdAndUsuario_RolOrderByNombreVisibleAsc(Long empresaId, Rol rol);

    /** Empleado por id, acotado a la empresa del usuario. */
    Optional<Empleado> findByIdAndSucursal_Empresa_Id(Long id, Long empresaId);
}
