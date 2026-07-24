package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Empresa;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EmpresaResponse {

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
    private Boolean estado;
    private LocalDateTime fechaCreacion;

    /** Estado de validación: APROBADA / PENDIENTE / RECHAZADA. */
    private String estadoValidacion;
    private String motivoRechazo;
    private boolean constanciaCargada;

    /** True si es la empresa que el dueño está gestionando actualmente. */
    private boolean activa;

    public static EmpresaResponse fromEntity(Empresa empresa) {
        return fromEntity(empresa, false);
    }

    public static EmpresaResponse fromEntity(Empresa empresa, boolean activa) {
        return EmpresaResponse.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .nombreFantasia(empresa.getNombreFantasia())
                .rubro(empresa.getRubro())
                .cuit(empresa.getCuit())
                .provincia(empresa.getProvincia())
                .calle(empresa.getCalle())
                .numeracion(empresa.getNumeracion())
                .emailContacto(empresa.getEmailContacto())
                .telefono(empresa.getTelefono())
                .estado(empresa.getEstado())
                .fechaCreacion(empresa.getFechaCreacion())
                .estadoValidacion(empresa.getEstadoValidacion() != null ? empresa.getEstadoValidacion().name() : "APROBADA")
                .motivoRechazo(empresa.getMotivoRechazo())
                .constanciaCargada(empresa.getConstanciaNombre() != null)
                .activa(activa)
                .build();
    }
}
