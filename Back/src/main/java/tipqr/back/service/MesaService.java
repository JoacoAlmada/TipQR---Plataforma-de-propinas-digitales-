package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.MesaRequest;
import tipqr.back.dto.MesaResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Mesa;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.MesaRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final QrService qrService;

    @Transactional(readOnly = true)
    public List<MesaResponse> listar(String emailUsuario, Long sucursalId) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        List<Mesa> mesas;
        if (sucursalId != null) {
            sucursalRepository.findByIdAndEmpresaId(sucursalId, empresa.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal", sucursalId));
            mesas = mesaRepository.findBySucursalIdOrderByNumeroAsc(sucursalId);
        } else {
            mesas = mesaRepository.findBySucursal_Empresa_IdOrderByNumeroAsc(empresa.getId());
        }
        return mesas.stream().map(MesaResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public MesaResponse obtenerPorId(Long id, String emailUsuario) {
        return MesaResponse.fromEntity(mesaPropia(id, emailUsuario));
    }

    @Transactional
    public MesaResponse crear(MesaRequest request, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        Sucursal sucursal = sucursalDeLaEmpresa(request.getSucursalId(), empresa);

        if (mesaRepository.existsBySucursalIdAndNumero(sucursal.getId(), request.getNumero())) {
            throw new DuplicateResourceException(
                    "Ya existe la mesa " + request.getNumero() + " en la sucursal " + sucursal.getNombre());
        }

        Mesa mesa = mesaRepository.save(Mesa.builder()
                .sucursal(sucursal)
                .numero(request.getNumero())
                .descripcion(request.getDescripcion())
                .estado(true)
                .build());

        // Alta automática del código QR de la mesa.
        qrService.generarParaMesa(mesa);

        return MesaResponse.fromEntity(mesa);
    }

    @Transactional
    public MesaResponse actualizar(Long id, MesaRequest request, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        Mesa mesa = mesaPropia(id, emailUsuario);
        Sucursal sucursal = sucursalDeLaEmpresa(request.getSucursalId(), empresa);

        if (mesaRepository.existsBySucursalIdAndNumeroAndIdNot(sucursal.getId(), request.getNumero(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe la mesa " + request.getNumero() + " en la sucursal " + sucursal.getNombre());
        }

        mesa.setNumero(request.getNumero());
        mesa.setDescripcion(request.getDescripcion());
        mesa.setSucursal(sucursal);
        return MesaResponse.fromEntity(mesaRepository.save(mesa));
    }

    @Transactional
    public MesaResponse cambiarEstado(Long id, boolean estado, String emailUsuario) {
        Mesa mesa = mesaPropia(id, emailUsuario);
        mesa.setEstado(estado);
        return MesaResponse.fromEntity(mesaRepository.save(mesa));
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

    private Mesa mesaPropia(Long id, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        return mesaRepository.findByIdAndSucursal_Empresa_Id(id, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
    }
}
