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
    private String rubro;
    private String cuit;
    private String emailContacto;
    private String telefono;
    private Boolean estado;
    private LocalDateTime fechaCreacion;

    public static EmpresaResponse fromEntity(Empresa empresa) {
        return EmpresaResponse.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .rubro(empresa.getRubro())
                .cuit(empresa.getCuit())
                .emailContacto(empresa.getEmailContacto())
                .telefono(empresa.getTelefono())
                .estado(empresa.getEstado())
                .fechaCreacion(empresa.getFechaCreacion())
                .build();
    }
}
