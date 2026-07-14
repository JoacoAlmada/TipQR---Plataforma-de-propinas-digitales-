package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.NotificacionDestinatario;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionDestinatarioRepository extends JpaRepository<NotificacionDestinatario, Long> {

    // Bandeja del usuario: no leídas primero, luego por fecha desc.
    List<NotificacionDestinatario> findByUsuarioIdOrderByLeidaAscNotificacion_FechaCreacionDesc(Long usuarioId);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    Optional<NotificacionDestinatario> findByIdAndUsuarioId(Long id, Long usuarioId);
}
