package tipqr.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tipqr.back.dto.TurnoAbrirRequest;
import tipqr.back.dto.TurnoResponse;
import tipqr.back.entity.Empresa;
import tipqr.back.entity.GrupoPropina;
import tipqr.back.entity.Sucursal;
import tipqr.back.entity.Turno;
import tipqr.back.entity.Usuario;
import tipqr.back.exception.ResourceNotFoundException;
import tipqr.back.repository.GrupoPropinaRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.TurnoRepository;
import tipqr.back.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Turno activo de una sucursal: define el grupo que cobra las propinas de mesa.
 * Abrir un turno cierra automáticamente el anterior (a lo sumo uno activo por sucursal).
 */
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final SucursalRepository sucursalRepository;
    private final GrupoPropinaRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public TurnoResponse abrirTurno(TurnoAbrirRequest request, String emailUsuario) {
        Usuario usuario = usuario(emailUsuario);
        Empresa empresa = empresaDe(usuario);
        Sucursal sucursal = sucursalRepository.findByIdAndEmpresaId(request.getSucursalId(), empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", request.getSucursalId()));
        GrupoPropina grupo = grupoRepository.findByIdAndSucursal_Empresa_Id(request.getGrupoId(), empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de propina", request.getGrupoId()));

        if (!grupo.getSucursal().getId().equals(sucursal.getId())) {
            throw new IllegalArgumentException("El grupo debe pertenecer a la sucursal del turno");
        }

        // Cerrar el turno activo anterior, si lo hay.
        turnoRepository.findBySucursalIdAndActivoTrue(sucursal.getId()).ifPresent(this::cerrar);

        Turno turno = Turno.builder()
                .sucursal(sucursal)
                .grupoPropina(grupo)
                .abiertoPor(usuario)
                .nombre(request.getNombre())
                .activo(true)
                .build();
        return TurnoResponse.fromEntity(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cerrarTurnoActivo(Long sucursalId, String emailUsuario) {
        validarSucursalDeLaEmpresa(sucursalId, emailUsuario);
        Turno turno = turnoRepository.findBySucursalIdAndActivoTrue(sucursalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay un turno activo en la sucursal " + sucursalId));
        return TurnoResponse.fromEntity(cerrar(turno));
    }

    @Transactional(readOnly = true)
    public TurnoResponse turnoActivo(Long sucursalId, String emailUsuario) {
        validarSucursalDeLaEmpresa(sucursalId, emailUsuario);
        return turnoRepository.findBySucursalIdAndActivoTrue(sucursalId)
                .map(TurnoResponse::fromEntity)
                .orElse(null);
    }

    /** Grupo activo de la sucursal (para repartir las propinas de mesa). Uso interno. */
    @Transactional(readOnly = true)
    public Optional<GrupoPropina> grupoActivo(Long sucursalId) {
        return turnoRepository.findBySucursalIdAndActivoTrue(sucursalId).map(Turno::getGrupoPropina);
    }

    // ── Helpers ─────────────────────────────────────────

    private Turno cerrar(Turno turno) {
        turno.setActivo(false);
        turno.setFechaCierre(LocalDateTime.now());
        return turnoRepository.save(turno);
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Empresa empresaDe(Usuario usuario) {
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException("El usuario no tiene una empresa asociada");
        }
        return empresa;
    }

    private void validarSucursalDeLaEmpresa(Long sucursalId, String emailUsuario) {
        Empresa empresa = empresaDe(usuario(emailUsuario));
        sucursalRepository.findByIdAndEmpresaId(sucursalId, empresa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", sucursalId));
    }
}
