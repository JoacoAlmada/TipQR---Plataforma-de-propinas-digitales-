package tipqr.back.entity.enums;

/**
 * Estado de validación de una empresa.
 *
 * APROBADA  → operativa (las del registro/seed y las que el superadmin aprobó).
 * PENDIENTE → dada de alta por un dueño ya validado; espera revisión del superadmin.
 * RECHAZADA → el superadmin la rechazó (con motivo).
 *
 * Solo una empresa APROBADA se puede gestionar (activar).
 */
public enum EstadoValidacionEmpresa {
    APROBADA,
    PENDIENTE,
    RECHAZADA
}
