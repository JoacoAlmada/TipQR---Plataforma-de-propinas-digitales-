package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Turno;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    /** Turno actualmente abierto de una sucursal (a lo sumo uno). */
    Optional<Turno> findBySucursalIdAndActivoTrue(Long sucursalId);

    /** Turnos activos de toda la empresa (uno por sucursal con turno abierto). */
    List<Turno> findBySucursal_Empresa_IdAndActivoTrueOrderBySucursal_NombreAsc(Long empresaId);

    /** Scoping multi-tenant. */
    Optional<Turno> findByIdAndSucursal_Empresa_Id(Long id, Long empresaId);
}
