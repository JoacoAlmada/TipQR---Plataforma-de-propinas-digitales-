package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.ConfigurarPorcentajesRequest;
import tipqr.back.dto.GrupoPropinaRequest;
import tipqr.back.dto.GrupoPropinaResponse;
import tipqr.back.dto.MiembroGrupoResponse;
import tipqr.back.entity.Empleado;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.GrupoPropinaEmpleado;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.entity.enums.TipoDistribucion;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpleadoRepository;
import tipqr.back.repository.GrupoPropinaEmpleadoRepository;
import tipqr.back.repository.GrupoPropinaRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GrupoPropinaService {

    private final GrupoPropinaRepository grupoRepository;
    private final GrupoPropinaEmpleadoRepository miembroRepository;
    private final EmpleadoRepository empleadoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<GrupoPropinaResponse> listar(String emailUsuario, Long sucursalId) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        List<GrupoPropina> grupos;
        if (sucursalId != null) {
            sucursalRepository.findByIdAndEmpresaId(sucursalId, empresa.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal", sucursalId));
            grupos = grupoRepository.findBySucursalIdOrderByNombreAsc(sucursalId);
        } else {
            grupos = grupoRepository.findBySucursal_Empresa_IdOrderByNombreAsc(empresa.getId());
        }
        return grupos.stream().map(GrupoPropinaResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public GrupoPropinaResponse obtenerPorId(Long id, String emailUsuario) {
        return GrupoPropinaResponse.fromEntity(grupoPropio(id, emailUsuario));
    }

    @Transactional
    public GrupoPropinaResponse crear(GrupoPropinaRequest request, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        Sucursal sucursal = sucursalDeLaEmpresa(request.getSucursalId(), empresa);
        String nombre = request.getNombre().trim();

        if (grupoRepository.existsByNombreIgnoreCaseAndSucursalId(nombre, sucursal.getId())) {
            throw new DuplicateResourceException(
                    "Ya existe un grupo con el nombre " + nombre + " en la sucursal " + sucursal.getNombre());
        }

        GrupoPropina grupo = GrupoPropina.builder()
                .sucursal(sucursal)
                .nombre(nombre)
                .descripcion(request.getDescripcion())
                .tipoGrupo(request.getTipoGrupo())
                .estado(true)
                .build();
        return GrupoPropinaResponse.fromEntity(grupoRepository.save(grupo));
    }

    @Transactional
    public GrupoPropinaResponse actualizar(Long id, GrupoPropinaRequest request, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        GrupoPropina grupo = grupoPropio(id, emailUsuario);
        Sucursal sucursal = sucursalDeLaEmpresa(request.getSucursalId(), empresa);
        String nombre = request.getNombre().trim();

        if (grupoRepository.existsByNombreIgnoreCaseAndSucursalIdAndIdNot(nombre, sucursal.getId(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe un grupo con el nombre " + nombre + " en la sucursal " + sucursal.getNombre());
        }

        grupo.setNombre(nombre);
        grupo.setDescripcion(request.getDescripcion());
        grupo.setTipoGrupo(request.getTipoGrupo());
        grupo.setSucursal(sucursal);
        return GrupoPropinaResponse.fromEntity(grupoRepository.save(grupo));
    }

    @Transactional
    public GrupoPropinaResponse cambiarEstado(Long id, boolean estado, String emailUsuario) {
        GrupoPropina grupo = grupoPropio(id, emailUsuario);
        grupo.setEstado(estado);
        return GrupoPropinaResponse.fromEntity(grupoRepository.save(grupo));
    }

    // ── Miembros del grupo (relación N:N con empleados) ──

    @Transactional(readOnly = true)
    public List<MiembroGrupoResponse> listarMiembros(Long grupoId, String emailUsuario) {
        grupoPropio(grupoId, emailUsuario);
        return miembroRepository.findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(grupoId)
                .stream().map(MiembroGrupoResponse::fromEntity).toList();
    }

    @Transactional
    public void agregarEmpleado(Long grupoId, Long empleadoId, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        GrupoPropina grupo = grupoRepository.findByIdAndSucursal_Empresa_Id(grupoId, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de propina", grupoId));
        Empleado empleado = empleadoRepository.findByIdAndSucursal_Empresa_Id(empleadoId, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", empleadoId));

        if (!empleado.getSucursal().getId().equals(grupo.getSucursal().getId())) {
            throw new IllegalArgumentException(
                    "El empleado debe pertenecer a la misma sucursal que el grupo");
        }
        if (miembroRepository.existsByGrupoPropinaIdAndEmpleadoId(grupoId, empleadoId)) {
            throw new DuplicateResourceException("El empleado ya está en el grupo");
        }

        miembroRepository.save(GrupoPropinaEmpleado.builder()
                .grupoPropina(grupo)
                .empleado(empleado)
                .activo(true)
                .build());
    }

    @Transactional
    public void removerEmpleado(Long grupoId, Long empleadoId, String emailUsuario) {
        grupoPropio(grupoId, emailUsuario);
        GrupoPropinaEmpleado miembro = miembroRepository
                .findByGrupoPropinaIdAndEmpleadoId(grupoId, empleadoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El empleado no pertenece a este grupo"));
        miembroRepository.delete(miembro);
    }

    // ── Distribución del grupo (equitativo / por porcentajes) ──

    /**
     * Configura el reparto por porcentajes: setea el % de cada miembro (deben sumar 100) y pone
     * el grupo en modo PORCENTAJE. Requiere un porcentaje para cada miembro activo del grupo.
     */
    @Transactional
    public GrupoPropinaResponse configurarPorcentajes(Long grupoId, ConfigurarPorcentajesRequest req,
                                                       String emailUsuario) {
        GrupoPropina grupo = grupoPropio(grupoId, emailUsuario);
        List<GrupoPropinaEmpleado> miembros = miembroRepository
                .findByGrupoPropinaIdOrderByEmpleado_NombreVisibleAsc(grupoId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo())).toList();

        if (miembros.isEmpty()) {
            throw new IllegalArgumentException("El grupo no tiene miembros para configurar porcentajes");
        }

        Map<Long, Double> porEmpleado = new HashMap<>();
        BigDecimal suma = BigDecimal.ZERO;
        for (ConfigurarPorcentajesRequest.ItemPorcentaje item : req.getPorcentajes()) {
            if (item.getPorcentaje() == null || item.getPorcentaje() <= 0) {
                throw new IllegalArgumentException("Cada porcentaje debe ser mayor a 0");
            }
            porEmpleado.put(item.getEmpleadoId(), item.getPorcentaje());
            suma = suma.add(BigDecimal.valueOf(item.getPorcentaje()));
        }

        if (suma.subtract(BigDecimal.valueOf(100)).abs().compareTo(new BigDecimal("0.5")) > 0) {
            throw new IllegalArgumentException("Los porcentajes deben sumar 100 (suman " + suma + ")");
        }

        for (GrupoPropinaEmpleado m : miembros) {
            Double pct = porEmpleado.get(m.getEmpleado().getId());
            if (pct == null) {
                throw new IllegalArgumentException(
                        "Falta el porcentaje de " + m.getEmpleado().getNombreVisible());
            }
            m.setPorcentajeDistribucion(pct);
            miembroRepository.save(m);
        }

        grupo.setTipoDistribucion(TipoDistribucion.PORCENTAJE);
        return GrupoPropinaResponse.fromEntity(grupoRepository.save(grupo));
    }

    /** Vuelve el grupo a reparto equitativo (partes iguales). */
    @Transactional
    public GrupoPropinaResponse usarEquitativo(Long grupoId, String emailUsuario) {
        GrupoPropina grupo = grupoPropio(grupoId, emailUsuario);
        grupo.setTipoDistribucion(TipoDistribucion.EQUITATIVO);
        return GrupoPropinaResponse.fromEntity(grupoRepository.save(grupo));
    }

    // ── Helpers ─────────────────────────────────────────

    private Empresa empresaDelUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }

    private Sucursal sucursalDeLaEmpresa(Long sucursalId, Empresa empresa) {
        return sucursalRepository.findByIdAndEmpresaId(sucursalId, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", sucursalId));
    }

    private GrupoPropina grupoPropio(Long id, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        return grupoRepository.findByIdAndSucursal_Empresa_Id(id, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de propina", id));
    }
}
