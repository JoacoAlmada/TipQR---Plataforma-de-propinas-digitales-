package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tipqr.back.entity.ReporteAutomatico;

import java.util.List;

public interface ReporteAutomaticoRepository extends JpaRepository<ReporteAutomatico, Long> {

    List<ReporteAutomatico> findByEmpresaIdOrderByFechaGeneracionDesc(Long empresaId);
}
