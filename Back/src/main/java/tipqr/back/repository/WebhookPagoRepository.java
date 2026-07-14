package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.WebhookPago;

@Repository
public interface WebhookPagoRepository extends JpaRepository<WebhookPago, Long> {
}
