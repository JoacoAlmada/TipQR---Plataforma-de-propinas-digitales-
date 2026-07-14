package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.DistribucionPropina;

import java.util.List;

@Repository
public interface DistribucionPropinaRepository extends JpaRepository<DistribucionPropina, Long> {

    boolean existsByOrdenPropinaId(Long ordenPropinaId);

    // Historial del empleado: lo que le tocó por reparto grupal.
    List<DistribucionPropina> findByEmpleadoIdOrderByOrdenPropina_FechaPagoDesc(Long empleadoId);

    // Ranking del dueño: todas las distribuciones de la empresa.
    List<DistribucionPropina> findByOrdenPropina_Sucursal_Empresa_Id(Long empresaId);
}
