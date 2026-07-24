package tipqr.back.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Estado + datos de un registro para retomarlo (corregir y reenviar tras un rechazo). */
@Getter
@Builder
public class RegistroResumenResponse {

    private String registroToken;
    private String estadoCuenta;
    private String motivoRechazo;

    // Datos del dueño
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String cuit;
    private String dni;

    // Datos del comercio
    private String nombreEmpresa;
    private String nombreFantasia;
    private String provincia;
    private String calle;
    private String numeracion;
    private String empresaCuit;
    private String rubro;

    // Tipos de documento ya cargados
    private List<String> documentosCargados;
}
