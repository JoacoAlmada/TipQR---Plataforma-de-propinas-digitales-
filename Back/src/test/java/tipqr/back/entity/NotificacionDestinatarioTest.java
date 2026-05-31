package tipqr.back.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionDestinatarioTest {

    @Test
    void builder_creaDestinatarioConCampos() {
        Notificacion notificacion = Notificacion.builder().titulo("Test").build();
        Usuario usuario = Usuario.builder().nombre("Juan").build();

        NotificacionDestinatario nd = NotificacionDestinatario.builder()
                .notificacion(notificacion)
                .usuario(usuario)
                .build();

        assertEquals(notificacion, nd.getNotificacion());
        assertEquals(usuario, nd.getUsuario());
    }

    @Test
    void leidaPorDefecto_esFalse() {
        NotificacionDestinatario nd = NotificacionDestinatario.builder().build();
        assertFalse(nd.getLeida());
    }

    @Test
    void marcarComoLeida_actualizaValores() {
        NotificacionDestinatario nd = NotificacionDestinatario.builder().build();
        LocalDateTime ahora = LocalDateTime.now();

        nd.setLeida(true);
        nd.setFechaLectura(ahora);

        assertTrue(nd.getLeida());
        assertEquals(ahora, nd.getFechaLectura());
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        NotificacionDestinatario nd = new NotificacionDestinatario();
        nd.setLeida(false);
        assertFalse(nd.getLeida());
    }
}
