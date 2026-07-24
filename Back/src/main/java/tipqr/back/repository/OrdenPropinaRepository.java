package tipqr.back.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tipqr.back.entity.OrdenPropina;
import tipqr.back.entity.enums.EstadoOrden;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenPropinaRepository extends JpaRepository<OrdenPropina, Long> {

    Optional<OrdenPropina> findByCodigo(String codigo);

    /**
     * Igual que findByCodigo pero tomando un lock de escritura sobre la fila. Se usa al conciliar
     * un pago para serializar el webhook y el retorno de MP (que llegan casi simultáneos) y evitar
     * que la propina se marque pagada —y se notifique— dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenPropina o where o.codigo = :codigo")
    Optional<OrdenPropina> findByCodigoParaConciliar(@Param("codigo") String codigo);

    boolean existsByCodigo(String codigo);

    // Scoping multi-tenant: una orden de otra empresa no se debe poder leer.
    Optional<OrdenPropina> findByCodigoAndSucursal_Empresa_Id(String codigo, Long empresaId);

    List<OrdenPropina> findBySucursal_Empresa_IdOrderByFechaCreacionDesc(Long empresaId);

    // Órdenes vencidas que siguen en un estado no final (para la expiración automática).
    List<OrdenPropina> findByEstadoInAndFechaExpiracionBefore(
            List<EstadoOrden> estados, LocalDateTime limite);

    // Historial del empleado: sus propinas individuales en un estado dado (ej. PAGADA).
    List<OrdenPropina> findByEmpleadoIdAndEstadoOrderByFechaPagoDesc(Long empleadoId, EstadoOrden estado);

    // Dashboard del dueño: todas las órdenes de la empresa en un estado dado.
    List<OrdenPropina> findBySucursal_Empresa_IdAndEstado(Long empresaId, EstadoOrden estado);

    // Reporte por período: órdenes de la empresa pagadas en un rango de fechas.
    List<OrdenPropina> findBySucursal_Empresa_IdAndEstadoAndFechaPagoBetween(
            Long empresaId, EstadoOrden estado, LocalDateTime desde, LocalDateTime hasta);
}
