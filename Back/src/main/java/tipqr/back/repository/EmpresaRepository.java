package tipqr.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByCuit(String cuit);

    boolean existsByCuitAndIdNot(String cuit, Long id);
}
