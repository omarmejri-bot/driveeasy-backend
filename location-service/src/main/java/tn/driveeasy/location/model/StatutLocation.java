package tn.driveeasy.location.model;

/**
 * Cycle de vie d'une location :
 * RESERVEE -> EN_COURS (départ) -> TERMINEE (retour)
 * RESERVEE -> ANNULEE (annulation)
 */
public enum StatutLocation {
    RESERVEE, EN_COURS, TERMINEE, ANNULEE
}
