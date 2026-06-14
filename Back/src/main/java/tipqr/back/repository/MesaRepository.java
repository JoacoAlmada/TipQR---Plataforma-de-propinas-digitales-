package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Mesa;

import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findBySucursal_Empresa_IdOrderByNumeroAsc(Long empresaId);

    List<Mesa> findBySucursalIdOrderByNumeroAsc(Long sucursalId);

    Optional<Mesa> findByIdAndSucursal_Empresa_Id(Long id, Long empresaId);

    boolean existsBySucursalIdAndNumero(Long sucursalId, Integer numero);

    boolean existsBySucursalIdAndNumeroAndIdNot(Long sucursalId, Integer numero, Long id);
}
