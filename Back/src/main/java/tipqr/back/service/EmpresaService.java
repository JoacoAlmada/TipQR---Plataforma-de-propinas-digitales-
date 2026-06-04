package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.EmpresaRequest;
import tipqr.back.dto.EmpresaResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpresaRepository;
import tipqr.back.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Empresa del usuario autenticado. Listado acotado (un solo comercio por usuario).
     */
    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar(String emailUsuario) {
        return List.of(EmpresaResponse.fromEntity(empresaDelUsuario(emailUsuario)));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse miEmpresa(String emailUsuario) {
        return EmpresaResponse.fromEntity(empresaDelUsuario(emailUsuario));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPorId(Long id, String emailUsuario) {
        return EmpresaResponse.fromEntity(empresaPropia(id, emailUsuario));
    }

    @Transactional
    public EmpresaResponse actualizar(Long id, EmpresaRequest request, String emailUsuario) {
        Empresa empresa = empresaPropia(id, emailUsuario);
        validarCuitUnico(request.getCuit(), empresa.getId());

        empresa.setNombre(request.getNombre());
        empresa.setRubro(request.getRubro());
        empresa.setCuit(normalizar(request.getCuit()));
        empresa.setEmailContacto(request.getEmailContacto());
        empresa.setTelefono(request.getTelefono());

        return EmpresaResponse.fromEntity(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponse cambiarEstado(Long id, boolean estado, String emailUsuario) {
        Empresa empresa = empresaPropia(id, emailUsuario);
        empresa.setEstado(estado);
        return EmpresaResponse.fromEntity(empresaRepository.save(empresa));
    }

    /**
     * Empresa asociada al usuario autenticado (resuelta desde el contexto, no del request).
     */
    private Empresa empresaDelUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }

    /**
     * Valida que la empresa solicitada por id sea la del usuario. Si no, 404 (no se
     * revela la existencia de empresas ajenas).
     */
    private Empresa empresaPropia(Long id, String emailUsuario) {
        Empresa empresa = empresaDelUsuario(emailUsuario);
        if (!empresa.getId().equals(id)) {
            throw new ResourceNotFoundException("Empresa", id);
        }
        return empresa;
    }

    private void validarCuitUnico(String cuit, Long idActual) {
        String cuitNormalizado = normalizar(cuit);
        if (cuitNormalizado == null) {
            return;
        }
        if (empresaRepository.existsByCuitAndIdNot(cuitNormalizado, idActual)) {
            throw new DuplicateResourceException("Ya existe una empresa con el CUIT " + cuitNormalizado);
        }
    }

    private String normalizar(String cuit) {
        if (cuit == null || cuit.isBlank()) {
            return null;
        }
        return cuit.trim();
    }
}
