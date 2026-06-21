package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.CodigoQR;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodigoQRRepository extends JpaRepository<CodigoQR, Long> {

    Optional<CodigoQR> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    // Idempotencia: un QR por mesa y por empleado.
    Optional<CodigoQR> findByMesaId(Long mesaId);

    Optional<CodigoQR> findByEmpleadoId(Long empleadoId);

    // Scoping multi-tenant.
    Optional<CodigoQR> findByIdAndSucursal_Empresa_Id(Long id, Long empresaId);

    List<CodigoQR> findBySucursal_Empresa_IdOrderByFechaCreacionDesc(Long empresaId);

    List<CodigoQR> findBySucursalIdOrderByFechaCreacionDesc(Long sucursalId);
}
