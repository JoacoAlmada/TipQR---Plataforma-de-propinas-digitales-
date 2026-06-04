package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SolicitudDetalleResponse {

    // Datos del dueño
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String cuit;
    private String dni;
    private String estadoCuenta;
    private LocalDateTime fechaSolicitud;

    // Datos del comercio
    private String empresaNombre;
    private String nombreFantasia;
    private String rubro;
    private String empresaCuit;
    private String provincia;
    private String calle;
    private String numeracion;

    // Documentos adjuntos (metadata; el binario se descarga aparte)
    private List<DocumentoMetaResponse> documentos;

    public static SolicitudDetalleResponse build(Usuario u, List<DocumentoMetaResponse> docs) {
        Empresa e = u.getEmpresa();
        return SolicitudDetalleResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .cuit(u.getCuit())
                .dni(u.getDni())
                .estadoCuenta(u.getEstadoCuenta().name())
                .fechaSolicitud(u.getFechaCreacion())
                .empresaNombre(e != null ? e.getNombre() : null)
                .nombreFantasia(e != null ? e.getNombreFantasia() : null)
                .rubro(e != null ? e.getRubro() : null)
                .empresaCuit(e != null ? e.getCuit() : null)
                .provincia(e != null ? e.getProvincia() : null)
                .calle(e != null ? e.getCalle() : null)
                .numeracion(e != null ? e.getNumeracion() : null)
                .documentos(docs)
                .build();
    }
}
