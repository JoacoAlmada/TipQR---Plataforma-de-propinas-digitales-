package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.GrupoPropina;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoPropinaRepository extends JpaRepository<GrupoPropina, Long> {

    List<GrupoPropina> findBySucursal_Empresa_IdOrderByNombreAsc(Long empresaId);

    List<GrupoPropina> findBySucursalIdOrderByNombreAsc(Long sucursalId);

    Optional<GrupoPropina> findByIdAndSucursal_Empresa_Id(Long id, Long empresaId);

    boolean existsByNombreIgnoreCaseAndSucursalId(String nombre, Long sucursalId);

    boolean existsByNombreIgnoreCaseAndSucursalIdAndIdNot(String nombre, Long sucursalId, Long id);
}
