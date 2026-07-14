package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Lo que ve el cliente al escanear el QR de una mesa: a quién puede dejarle la propina.
 * Si hay un turno activo, lista los mozos de ese turno (para elegir individual) y permite "al equipo".
 */
@Getter
@Builder
@AllArgsConstructor
public class MesaDestinatariosResponse {

    private String codigo;
    private String destinoNombre;
    private String sucursalNombre;
    private String empresaNombre;
    private boolean turnoActivo;
    private String grupoNombre;
    private List<Mozo> mozos;

    @Getter
    @AllArgsConstructor
    public static class Mozo {
        private Long empleadoId;
        private String nombre;
    }
}
