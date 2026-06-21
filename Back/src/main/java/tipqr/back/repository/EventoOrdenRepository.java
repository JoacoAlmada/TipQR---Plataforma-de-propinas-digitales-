package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.EventoOrden;

import java.util.List;

@Repository
public interface EventoOrdenRepository extends JpaRepository<EventoOrden, Long> {

    List<EventoOrden> findByOrdenPropinaIdOrderByFechaAsc(Long ordenPropinaId);
}
