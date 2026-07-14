package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Turno;

import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    /** Turno actualmente abierto de una sucursal (a lo sumo uno). */
    Optional<Turno> findBySucursalIdAndActivoTrue(Long sucursalId);

    /** Scoping multi-tenant. */
    Optional<Turno> findByIdAndSucursal_Empresa_Id(Long id, Long empresaId);
}
