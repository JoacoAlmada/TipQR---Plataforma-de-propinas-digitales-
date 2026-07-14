package tipqr.back.service.ia;

/**
 * Abstracción del proveedor de IA generativa. Permite cambiar de proveedor
 * (Gemini, Claude, etc.) sin tocar los servicios que lo consumen. Los agentes
 * consultan {@link #disponible()} y, si no hay proveedor configurado, aplican
 * una redacción local de respaldo (para que la demo y los tests funcionen sin key).
 */
public interface ProveedorIa {

    /** True si el proveedor está habilitado y tiene credenciales cargadas. */
    boolean disponible();

    /**
     * Genera texto a partir de una instrucción de sistema (rol/estilo) y un prompt.
     *
     * @param instruccionSistema instrucción de contexto (puede ser null)
     * @param prompt             pedido concreto del usuario
     * @param jsonMode           si true, se le pide al modelo responder JSON puro
     * @return el texto generado por el modelo
     * @throws IaException si el proveedor no está disponible o la llamada falla
     */
    String completar(String instruccionSistema, String prompt, boolean jsonMode);
}
