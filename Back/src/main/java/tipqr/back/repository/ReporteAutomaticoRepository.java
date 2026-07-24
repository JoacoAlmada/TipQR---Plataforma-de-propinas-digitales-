package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tipqr.back.entity.ReporteAutomatico;

import java.util.List;
import java.util.Optional;

public interface ReporteAutomaticoRepository extends JpaRepository<ReporteAutomatico, Long> {

    List<ReporteAutomatico> findByEmpresaIdOrderByFechaGeneracionDesc(Long empresaId);

    /** Reporte por id acotado a la empresa (scoping multi-tenant). */
    Optional<ReporteAutomatico> findByIdAndEmpresaId(Long id, Long empresaId);
}
