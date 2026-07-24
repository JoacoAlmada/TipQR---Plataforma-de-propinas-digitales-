package tipqr.back.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Porcentaje de distribución por miembro del grupo. Deben sumar 100. */
@Getter @Setter
public class ConfigurarPorcentajesRequest {

    @NotEmpty(message = "Indicá el porcentaje de cada miembro")
    private List<ItemPorcentaje> porcentajes;

    @Getter @Setter
    public static class ItemPorcentaje {
        @NotNull(message = "Falta el empleado")
        private Long empleadoId;

        @NotNull(message = "Falta el porcentaje")
        private Double porcentaje;
    }
}
