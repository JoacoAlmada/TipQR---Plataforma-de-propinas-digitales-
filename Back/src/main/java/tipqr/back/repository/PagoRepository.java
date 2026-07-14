package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Pago;

import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByOrdenPropinaId(Long ordenPropinaId);

    Optional<Pago> findByOrdenPropinaCodigo(String codigo);

    Optional<Pago> findByExternalPaymentId(String externalPaymentId);
}
