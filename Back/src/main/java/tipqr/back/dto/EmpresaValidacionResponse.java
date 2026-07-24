package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;

import java.time.LocalDateTime;

/** Empresa pendiente de validación, con datos del propietario, para el panel del superadmin. */
@Getter
@Builder
@AllArgsConstructor
public class EmpresaValidacionResponse {

    private Long id;
    private String nombre;
    private String nombreFantasia;
    private String rubro;
    private String cuit;
    private String provincia;
    private String calle;
    private String numeracion;
    private String emailContacto;
    private String telefono;
    private String estadoValidacion;
    private String motivoRechazo;
    private LocalDateTime fechaCreacion;

    // Constancia de AFIP
    private boolean constanciaCargada;
    private String constanciaNombre;
    private String constanciaContentType;

    // Propietario (dueño ya validado que la dio de alta)
    private String propietarioNombre;
    private String propietarioApellido;
    private String propietarioEmail;
    private String propietarioTelefono;

    public static EmpresaValidacionResponse fromEntity(Empresa e) {
        Usuario p = e.getPropietario();
        return EmpresaValidacionResponse.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .nombreFantasia(e.getNombreFantasia())
                .rubro(e.getRubro())
                .cuit(e.getCuit())
                .provincia(e.getProvincia())
                .calle(e.getCalle())
                .numeracion(e.getNumeracion())
                .emailContacto(e.getEmailContacto())
                .telefono(e.getTelefono())
                .estadoValidacion(e.getEstadoValidacion() != null ? e.getEstadoValidacion().name() : null)
                .motivoRechazo(e.getMotivoRechazo())
                .fechaCreacion(e.getFechaCreacion())
                .constanciaCargada(e.getConstanciaNombre() != null)
                .constanciaNombre(e.getConstanciaNombre())
                .constanciaContentType(e.getConstanciaContentType())
                .propietarioNombre(p != null ? p.getNombre() : null)
                .propietarioApellido(p != null ? p.getApellido() : null)
                .propietarioEmail(p != null ? p.getEmail() : null)
                .propietarioTelefono(p != null ? p.getTelefono() : null)
                .build();
    }
}
