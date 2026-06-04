package tipqr.back.entity.enums;

/**
 * Ciclo de vida de la cuenta de un dueño durante el onboarding.
 *
 * CREADA               → registrada, email sin verificar.
 * VERIFICADA           → email verificado (puede continuar el alta).
 * PENDIENTE_VALIDACION → completó datos + documentos y envió a validar.
 * APROBADA             → el superadmin la aprobó; puede operar el sistema.
 * RECHAZADA            → el superadmin la rechazó.
 */
public enum EstadoCuenta {
    CREADA,
    VERIFICADA,
    PENDIENTE_VALIDACION,
    APROBADA,
    RECHAZADA
}
