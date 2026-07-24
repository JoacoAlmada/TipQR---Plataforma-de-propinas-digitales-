package tipqr.back.service.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tipqr.back.dto.RedaccionNotificacionResponse;
import tipqr.back.entity.enums.CategoriaNotificacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgenteNotificacionServiceTest {

    @Mock private ProveedorIa proveedorIa;

    private AgenteNotificacionService servicio() {
        return new AgenteNotificacionService(proveedorIa, new ObjectMapper());
    }

    @Test
    void redactar_conIa_parseaElJsonYMarcaGeneradoPorIa() {
        when(proveedorIa.disponible()).thenReturn(true);
        when(proveedorIa.completar(anyString(), anyString(), eq(false))).thenReturn(
                "{\"titulo\":\"Reunión de equipo\",\"mensaje\":\"Mañana hay reunión a las 9.\",\"categoria\":\"HORARIO\"}");

        RedaccionNotificacionResponse r = servicio().redactar("che avisá que mañana reunión 9am");

        assertTrue(r.isGeneradoPorIa());
        assertEquals("Reunión de equipo", r.getTitulo());
        assertEquals("Mañana hay reunión a las 9.", r.getMensaje());
        assertEquals(CategoriaNotificacion.HORARIO, r.getCategoria());
    }

    @Test
    void redactar_conJsonEnvueltoEnFences_igualLoParsea() {
        when(proveedorIa.disponible()).thenReturn(true);
        when(proveedorIa.completar(anyString(), anyString(), eq(false))).thenReturn(
                "```json\n{\"titulo\":\"Falta stock\",\"mensaje\":\"Reponer servilletas.\",\"categoria\":\"STOCK\"}\n```");

        RedaccionNotificacionResponse r = servicio().redactar("faltan servilletas, reponer");

        assertTrue(r.isGeneradoPorIa());
        assertEquals(CategoriaNotificacion.STOCK, r.getCategoria());
    }

    @Test
    void redactar_categoriaInvalidaDeLaIa_seDeducePorPalabras() {
        when(proveedorIa.disponible()).thenReturn(true);
        when(proveedorIa.completar(anyString(), anyString(), eq(false))).thenReturn(
                "{\"titulo\":\"Aviso\",\"mensaje\":\"Recordá el pago de sueldos.\",\"categoria\":\"CUALQUIERA\"}");

        RedaccionNotificacionResponse r = servicio().redactar("recordá el pago de sueldos");

        assertEquals(CategoriaNotificacion.PAGOS, r.getCategoria());
    }

    @Test
    void redactar_sinProveedor_usaRespaldoLocal() {
        when(proveedorIa.disponible()).thenReturn(false);

        RedaccionNotificacionResponse r = servicio().redactar("mañana el turno noche entra a las 18");

        assertFalse(r.isGeneradoPorIa());
        assertFalse(r.getTitulo().isBlank());
        assertTrue(r.getMensaje().endsWith("."));
        assertEquals(CategoriaNotificacion.HORARIO, r.getCategoria());
        verify(proveedorIa, never()).completar(anyString(), anyString(), anyBoolean());
    }

    @Test
    void redactar_siLaIaFalla_caeAlRespaldoLocal() {
        when(proveedorIa.disponible()).thenReturn(true);
        when(proveedorIa.completar(anyString(), anyString(), eq(false)))
                .thenThrow(new IaException("timeout"));

        RedaccionNotificacionResponse r = servicio().redactar("reponer mercadería del depósito");

        assertFalse(r.isGeneradoPorIa());
        assertEquals(CategoriaNotificacion.STOCK, r.getCategoria());
    }

    @Test
    void redactar_instruccionVacia_lanzaError() {
        assertThrows(IaException.class, () -> servicio().redactar("   "));
    }
}
