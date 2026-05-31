package tipqr.back.entity;

import org.junit.jupiter.api.Test;
import tipqr.back.entity.enums.TipoDestinoQR;

import static org.junit.jupiter.api.Assertions.*;

class CodigoQRTest {

    @Test
    void builder_creaCodigoQRConCampos() {
        Sucursal sucursal = Sucursal.builder().nombre("Centro").build();
        CodigoQR qr = CodigoQR.builder()
                .codigo("ABC123")
                .tipoDestino(TipoDestinoQR.EMPLEADO)
                .sucursal(sucursal)
                .url("https://tipqr.com/p/ABC123")
                .build();

        assertEquals("ABC123", qr.getCodigo());
        assertEquals(TipoDestinoQR.EMPLEADO, qr.getTipoDestino());
        assertEquals("https://tipqr.com/p/ABC123", qr.getUrl());
    }

    @Test
    void activoPorDefecto_esTrue() {
        CodigoQR qr = CodigoQR.builder().codigo("X").tipoDestino(TipoDestinoQR.MESA).build();
        assertTrue(qr.getActivo());
    }

    @Test
    void tiposDestino_sonValidos() {
        assertEquals(4, TipoDestinoQR.values().length);
        assertNotNull(TipoDestinoQR.valueOf("EMPLEADO"));
        assertNotNull(TipoDestinoQR.valueOf("MESA"));
        assertNotNull(TipoDestinoQR.valueOf("GRUPO"));
        assertNotNull(TipoDestinoQR.valueOf("SUCURSAL"));
    }

    @Test
    void noArgsConstructorYSetters_funcionan() {
        CodigoQR qr = new CodigoQR();
        qr.setCodigo("XYZ789");
        qr.setTipoDestino(TipoDestinoQR.GRUPO);

        assertEquals("XYZ789", qr.getCodigo());
        assertEquals(TipoDestinoQR.GRUPO, qr.getTipoDestino());
    }
}
