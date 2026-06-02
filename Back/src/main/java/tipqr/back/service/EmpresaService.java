package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.EmpresaRequest;
import tipqr.back.dto.EmpresaResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.exception.DuplicateResourceException;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.EmpresaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresaRepository.findAll().stream()
                .map(EmpresaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPorId(Long id) {
        return EmpresaResponse.fromEntity(buscarEntidad(id));
    }

    @Transactional
    public EmpresaResponse crear(EmpresaRequest request) {
        validarCuitUnico(request.getCuit(), null);

        Empresa empresa = Empresa.builder()
                .nombre(request.getNombre())
                .rubro(request.getRubro())
                .cuit(normalizar(request.getCuit()))
                .emailContacto(request.getEmailContacto())
                .telefono(request.getTelefono())
                .estado(true)
                .build();

        return EmpresaResponse.fromEntity(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponse actualizar(Long id, EmpresaRequest request) {
        Empresa empresa = buscarEntidad(id);
        validarCuitUnico(request.getCuit(), id);

        empresa.setNombre(request.getNombre());
        empresa.setRubro(request.getRubro());
        empresa.setCuit(normalizar(request.getCuit()));
        empresa.setEmailContacto(request.getEmailContacto());
        empresa.setTelefono(request.getTelefono());

        return EmpresaResponse.fromEntity(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponse cambiarEstado(Long id, boolean estado) {
        Empresa empresa = buscarEntidad(id);
        empresa.setEstado(estado);
        return EmpresaResponse.fromEntity(empresaRepository.save(empresa));
    }

    private Empresa buscarEntidad(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
    }

    private void validarCuitUnico(String cuit, Long idActual) {
        String cuitNormalizado = normalizar(cuit);
        if (cuitNormalizado == null) {
            return;
        }
        boolean duplicado = (idActual == null)
                ? empresaRepository.existsByCuit(cuitNormalizado)
                : empresaRepository.existsByCuitAndIdNot(cuitNormalizado, idActual);
        if (duplicado) {
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
