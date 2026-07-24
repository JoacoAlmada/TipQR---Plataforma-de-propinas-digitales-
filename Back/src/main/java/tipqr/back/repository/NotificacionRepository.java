package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Notificacion;
import tipqr.back.entity.enums.OrigenNotificacion;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Notificaciones que un usuario redactó y envió (solapa "Enviadas"),
     * excluyendo los avisos automáticos del sistema (ej. "recibiste una propina").
     */
    List<Notificacion> findByCreadoPor_IdAndOrigenNotOrderByFechaCreacionDesc(
            Long usuarioId, OrigenNotificacion origen);
}
