package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.DocumentoRegistro;
import tipqr.back.entity.enums.TipoDocumento;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRegistroRepository extends JpaRepository<DocumentoRegistro, Long> {

    List<DocumentoRegistro> findByUsuarioId(Long usuarioId);

    Optional<DocumentoRegistro> findByUsuarioIdAndTipo(Long usuarioId, TipoDocumento tipo);

    long countByUsuarioId(Long usuarioId);
}
