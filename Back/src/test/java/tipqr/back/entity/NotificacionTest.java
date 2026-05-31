package tipqr.back.entity;

import org.junit.jupiter.api.Test;
import tipqr.back.entity.enums.CategoriaNotificacion;
import tipqr.back.entity.enums.OrigenNotificacion;
import tipqr.back.entity.enums.PrioridadNotificacion;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionTest {

    @Test
    void builder_creaNotificacionConCampos() {
        Empresa empresa = Empresa.builder().nombre("Empresa Test").build();
        Usuario creador = Usuario.builder().nombre("Admin").build();

        Notificacion n = Notificacion.builder()
                .empresa(empresa)
                .creadoPor(creador)
                .titulo("Reunión de equipo")
                .mensaje("Reunión mañana a las 10hs")
                .categoria(CategoriaNotificacion.OPERATIVA)
                .prioridad(PrioridadNotificacion.ALTA)
                .origen(OrigenNotificacion.MANUAL)
                .build();

        assertEquals("Reunión de equipo", n.getTitulo());
        assertEquals("Reunión mañana a las 10hs", n.getMensaje());
        assertEquals(CategoriaNotificacion.OPERATIVA, n.getCategoria());
        assertEquals(PrioridadNotificacion.ALTA, n.getPrioridad());
        assertEquals(OrigenNotificacion.MANUAL, n.getOrigen());
    }

    @Test
    void categoriasNotificacion_sonValidas() {
        assertEquals(5, CategoriaNotificacion.values().length);
    }

    @Test
    void prioridadesNotificacion_sonValidas() {
        assertEquals(3, PrioridadNotificacion.values().length);
        assertNotNull(PrioridadNotificacion.valueOf("BAJA"));
        assertNotNull(PrioridadNotificacion.valueOf("MEDIA"));
        assertNotNull(PrioridadNotificacion.valueOf("ALTA"));
    }

    @Test
    void origenNotificacion_sonValidos() {
        assertEquals(2, OrigenNotificacion.values().length);
        assertNotNull(OrigenNotificacion.valueOf("MANUAL"));
        assertNotNull(OrigenNotificacion.valueOf("AGENTE"));
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        Notificacion n = new Notificacion();
        n.setTitulo("Test");
        n.setMensaje("Mensaje de prueba");

        assertEquals("Test", n.getTitulo());
        assertEquals("Mensaje de prueba", n.getMensaje());
    }
}
