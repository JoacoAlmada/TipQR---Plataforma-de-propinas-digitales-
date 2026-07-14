package tipqr.back.dto;

import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.enums.CategoriaNotificacion;

/**
 * Borrador de aviso generado por el agente. El usuario lo revisa/edita y recién
 * después confirma el envío (control humano en el medio).
 */
@Getter
@Builder
public class RedaccionNotificacionResponse {

    private String titulo;
    private String mensaje;
    private CategoriaNotificacion categoria;

    /** True si lo redactó el proveedor de IA; false si se usó la redacción local de respaldo. */
    private boolean generadoPorIa;
}
