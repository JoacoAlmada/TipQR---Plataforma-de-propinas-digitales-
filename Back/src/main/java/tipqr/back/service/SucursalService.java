package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.SucursalRequest;
import tipqr.back.dto.SucursalResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpleadoRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar(String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        return sucursalRepository.findByEmpresaIdOrderByNombreAsc(empresa.getId())
                .stream().map(SucursalResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse obtenerPorId(Long id, String emailUsuario) {
        return SucursalResponse.fromEntity(sucursalPropia(id, emailUsuario));
    }

    @Transactional
    public SucursalResponse crear(SucursalRequest request, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        String nombre = request.getNombre().trim();
        if (sucursalRepository.existsByNombreIgnoreCaseAndEmpresaId(nombre, empresa.getId())) {
            throw new DuplicateResourceException("Ya existe una sucursal con el nombre " + nombre);
        }

        Sucursal sucursal = Sucursal.builder()
                .empresa(empresa)
                .nombre(nombre)
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .estado(true)
                .build();
        return SucursalResponse.fromEntity(sucursalRepository.save(sucursal));
    }

    @Transactional
    public SucursalResponse actualizar(Long id, SucursalRequest request, String emailUsuario) {
        Sucursal sucursal = sucursalPropia(id, emailUsuario);
        String nombre = request.getNombre().trim();
        if (sucursalRepository.existsByNombreIgnoreCaseAndEmpresaIdAndIdNot(
                nombre, sucursal.getEmpresa().getId(), id)) {
            throw new DuplicateResourceException("Ya existe una sucursal con el nombre " + nombre);
        }

        sucursal.setNombre(nombre);
        sucursal.setDireccion(request.getDireccion());
        sucursal.setTelefono(request.getTelefono());
        return SucursalResponse.fromEntity(sucursalRepository.save(sucursal));
    }

    @Transactional
    public SucursalResponse cambiarEstado(Long id, boolean estado, String emailUsuario) {
        Sucursal sucursal = sucursalPropia(id, emailUsuario);
        if (!estado && empleadoRepository.existsBySucursalIdAndEstadoTrue(id)) {
            throw new IllegalArgumentException(
                    "No se puede desactivar una sucursal con empleados activos");
        }
        sucursal.setEstado(estado);
        return SucursalResponse.fromEntity(sucursalRepository.save(sucursal));
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

    /** La sucursal debe pertenecer a la empresa del usuario; si no, 404. */
    private Sucursal sucursalPropia(Long id, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        return sucursalRepository.findByIdAndEmpresaId(id, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", id));
    }
}
