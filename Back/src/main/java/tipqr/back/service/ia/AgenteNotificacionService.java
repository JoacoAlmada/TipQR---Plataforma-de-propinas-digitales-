package tipqr.back.service.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tipqr.back.dto.RedaccionNotificacionResponse;
import tipqr.back.entity.enums.CategoriaNotificacion;

/**
 * Agente de notificaciones internas: convierte una instrucción informal del encargado
 * ("recordá al turno noche que mañana entran a las 18") en un aviso estructurado
 * (título, mensaje, categoría). El resultado es un BORRADOR: el usuario lo revisa,
 * edita y recién ahí confirma el envío — hay control humano antes de enviar.
 *
 * Si el proveedor de IA no está configurado o falla, se usa una redacción local de
 * respaldo para que la función siga operativa (útil para la demo y los tests).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgenteNotificacionService {

    private final ProveedorIa proveedorIa;
    private final ObjectMapper objectMapper;

    private static final String INSTRUCCION_SISTEMA = """
            Sos un asistente que ayuda a encargados de un comercio gastronómico a redactar
            avisos internos para su equipo. Convertís una instrucción informal en un aviso
            claro, cordial y profesional, en español rioplatense. No inventes datos que no
            estén en la instrucción.""";

    /** Genera un borrador de aviso a partir de la instrucción informal. */
    public RedaccionNotificacionResponse redactar(String instruccion) {
        String texto = instruccion == null ? "" : instruccion.trim();
        if (texto.isEmpty()) {
            throw new IaException("Escribí qué querés avisar.");
        }

        if (proveedorIa.disponible()) {
            try {
                return conIa(texto);
            } catch (IaException e) {
                log.warn("Agente de notificaciones: fallback local por error de IA: {}", e.getMessage());
            }
        }
        return respaldoLocal(texto);
    }

    // ── Camino con IA ─────────────────────────────────────────────────────────

    private RedaccionNotificacionResponse conIa(String instruccion) {
        String prompt = """
                Instrucción del encargado: "%s"

                Redactá el aviso y devolvé SOLO un JSON con esta forma exacta:
                {"titulo": "...", "mensaje": "...", "categoria": "..."}
                - titulo: máximo 60 caracteres, directo.
                - mensaje: 1 a 3 oraciones, tono cordial y profesional.
                - categoria: uno de OPERATIVA, STOCK, HORARIO, PAGOS, GENERAL (el que mejor aplique).
                """.formatted(instruccion);

        // Modo texto (no el JSON nativo de Gemini): su responseMimeType JSON devuelve UTF-8
        // roto en algunos acentos. Pedimos el JSON por prompt y lo extraemos nosotros.
        String raw = proveedorIa.completar(INSTRUCCION_SISTEMA, prompt, false);
        JsonNode json = parsear(raw);

        String titulo = recortar(textoDe(json, "titulo", "Aviso para el equipo"), 60);
        String mensaje = textoDe(json, "mensaje", instruccion);
        CategoriaNotificacion categoria = categoriaValida(json.path("categoria").asText(null), instruccion);

        return RedaccionNotificacionResponse.builder()
                .titulo(titulo)
                .mensaje(mensaje.trim())
                .categoria(categoria)
                .generadoPorIa(true)
                .build();
    }

    private JsonNode parsear(String raw) {
        String limpio = raw.trim();
        // El modelo puede envolver el JSON en ```json ... ``` o acompañarlo de texto:
        // extraemos el bloque entre la primera '{' y la última '}'.
        int ini = limpio.indexOf('{');
        int fin = limpio.lastIndexOf('}');
        if (ini >= 0 && fin > ini) {
            limpio = limpio.substring(ini, fin + 1);
        }
        try {
            return objectMapper.readTree(limpio);
        } catch (Exception e) {
            throw new IaException("La respuesta de la IA no es un JSON válido.", e);
        }
    }

    private String textoDe(JsonNode json, String campo, String porDefecto) {
        String v = json.path(campo).asText(null);
        return (v == null || v.isBlank()) ? porDefecto : v;
    }

    // ── Redacción local de respaldo ───────────────────────────────────────────

    private RedaccionNotificacionResponse respaldoLocal(String instruccion) {
        String limpio = capitalizar(instruccion.replaceAll("\\s+", " ").trim());
        if (!limpio.endsWith(".") && !limpio.endsWith("!") && !limpio.endsWith("?")) {
            limpio = limpio + ".";
        }
        return RedaccionNotificacionResponse.builder()
                .titulo(recortar(tituloDesde(instruccion), 60))
                .mensaje(limpio)
                .categoria(categoriaPorPalabras(instruccion))
                .generadoPorIa(false)
                .build();
    }

    private String tituloDesde(String instruccion) {
        String[] palabras = instruccion.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < palabras.length && i < 6; i++) {
            if (i > 0) sb.append(' ');
            sb.append(palabras[i]);
        }
        String t = capitalizar(sb.toString());
        return t.isBlank() ? "Aviso para el equipo" : t;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CategoriaNotificacion categoriaValida(String valor, String instruccion) {
        if (valor != null) {
            try {
                return CategoriaNotificacion.valueOf(valor.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // valor no reconocido: se deduce por palabras
            }
        }
        return categoriaPorPalabras(instruccion);
    }

    private CategoriaNotificacion categoriaPorPalabras(String texto) {
        String t = texto.toLowerCase();
        if (contiene(t, "turno", "horario", "reunion", "reunión", "mañana", "manana", "hora", "entran", "salen", "franco")) {
            return CategoriaNotificacion.HORARIO;
        }
        if (contiene(t, "stock", "falta", "faltan", "mercaderia", "mercadería", "pedido", "insumo", "repon")) {
            return CategoriaNotificacion.STOCK;
        }
        if (contiene(t, "pago", "sueldo", "propina", "cobro", "caja", "liquidacion", "liquidación")) {
            return CategoriaNotificacion.PAGOS;
        }
        if (contiene(t, "limpieza", "apertura", "cierre", "protocolo", "orden", "higiene", "uniforme")) {
            return CategoriaNotificacion.OPERATIVA;
        }
        return CategoriaNotificacion.GENERAL;
    }

    private boolean contiene(String texto, String... claves) {
        for (String c : claves) {
            if (texto.contains(c)) return true;
        }
        return false;
    }

    private String capitalizar(String s) {
        if (s == null || s.isBlank()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String recortar(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max).trim();
    }
}
