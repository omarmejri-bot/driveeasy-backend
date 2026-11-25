package tn.driveeasy.location.dto;

import jakarta.validation.constraints.PositiveOrZero;

/** Démarrage d'une location : kmDepart facultatif (sinon kilométrage actuel du véhicule). */
public record DepartRequest(
        @PositiveOrZero(message = "Le kilométrage de départ doit être positif ou nul")
        Integer kmDepart) {
}
