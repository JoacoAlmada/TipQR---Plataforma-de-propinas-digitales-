package tipqr.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tipqr.back.entity.*;
import tipqr.back.entity.enums.EstadoCuenta;
import tipqr.back.entity.enums.EstadoOrden;
import tipqr.back.entity.enums.Rol;
import tipqr.back.entity.enums.TipoEventoOrden;
import tipqr.back.entity.enums.TipoPropina;
import tipqr.back.repository.EventoOrdenRepository;
import tipqr.back.repository.OrdenPropinaRepository;
import tipqr.back.repository.SucursalRepository;
import tipqr.back.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final OrdenPropinaRepository ordenRepository;
    private final EventoOrdenRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        // Superadmin (dueño del producto) — único, no se registra por la app.
        if (!usuarioRepository.existsByEmail("superadmin@tipqr.com")) {
            Usuario superadmin = Usuario.builder()
                    .nombre("Joaquín")
                    .apellido("Almada")
                    .email("superadmin@tipqr.com")
                    .password(passwordEncoder.encode("superadmin2026"))
                    .rol(Rol.SUPERADMIN)
                    .estadoCuenta(EstadoCuenta.APROBADA)
                    .emailVerificado(true)
                    .build();
            usuarioRepository.save(superadmin);
            log.info("Superadmin creado: superadmin@tipqr.com / superadmin2026");
        }

        Empresa empresa = obtenerOCrearEmpresaDemo();
        seedOrdenesDemo(empresa);
    }

    /** Empresa + dueño demo (idempotente). Devuelve la empresa para sembrar datos operativos. */
    private Empresa obtenerOCrearEmpresaDemo() {
        Usuario admin = usuarioRepository.findByEmail("admin@tipqr.com").orElse(null);
        if (admin != null) {
            return admin.getEmpresa();
        }

        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa Demo");
        empresa.setRubro("Gastronomía");
        empresa.setCuit("30-12345678-9");
        empresa.setEmailContacto("demo@tipqr.com");
        empresa.setTelefono("3513000000");
        em.persist(empresa);

        admin = Usuario.builder()
                .nombre("Admin")
                .apellido("TipQR")
                .email("admin@tipqr.com")
                .password(passwordEncoder.encode("tipqr2026"))
                .rol(Rol.DUENO)
                .estadoCuenta(EstadoCuenta.APROBADA)
                .emailVerificado(true)
                .empresa(empresa)
                .build();
        usuarioRepository.save(admin);
        log.info("Usuario admin creado: admin@tipqr.com / tipqr2026");
        return empresa;
    }

    /**
     * Órdenes de propina demo para poder probar el endpoint público de estado y la expiración.
     * Idempotente: solo siembra si no existen. Se construyen directo (la creación real es vía QR).
     * - DEMOACTIVA: vigente (PENDIENTE_PAGO, vence dentro de 1 h).
     * - DEMOVENCIDA: ya vencida → la tarea programada la pasa a EXPIRADA en el próximo ciclo.
     */
    private void seedOrdenesDemo(Empresa empresa) {
        if (empresa == null || ordenRepository.existsByCodigo("DEMOACTIVA")) {
            return;
        }

        Sucursal sucursal = sucursalRepository.findByEmpresaIdOrderByNombreAsc(empresa.getId())
                .stream().findFirst().orElseGet(() -> {
                    Sucursal s = Sucursal.builder()
                            .empresa(empresa)
                            .nombre("Sucursal Centro Demo")
                            .direccion("Av. Colón 1000")
                            .telefono("3513111111")
                            .estado(true)
                            .build();
                    return sucursalRepository.save(s);
                });

        LocalDateTime ahora = LocalDateTime.now();
        crearOrdenDemo(sucursal, "DEMOACTIVA", new BigDecimal("2500.00"),
                EstadoOrden.PENDIENTE_PAGO, ahora.plusHours(1));
        crearOrdenDemo(sucursal, "DEMOVENCIDA", new BigDecimal("1500.00"),
                EstadoOrden.PENDIENTE_PAGO, ahora.minusMinutes(5));
        log.info("Órdenes demo creadas: DEMOACTIVA (vigente), DEMOVENCIDA (se expira sola)");
    }

    private void crearOrdenDemo(Sucursal sucursal, String codigo, BigDecimal monto,
                                EstadoOrden estado, LocalDateTime fechaExpiracion) {
        OrdenPropina orden = OrdenPropina.builder()
                .codigo(codigo)
                .sucursal(sucursal)
                .tipoPropina(TipoPropina.INDIVIDUAL)
                .monto(monto)
                .estado(estado)
                .fechaExpiracion(fechaExpiracion)
                .build();
        orden = ordenRepository.save(orden);
        eventoRepository.save(EventoOrden.builder()
                .ordenPropina(orden)
                .tipoEvento(TipoEventoOrden.ORDEN_CREADA)
                .descripcion("Orden demo sembrada")
                .build());
    }
}
