package tn.driveeasy.vehicule.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import tn.driveeasy.vehicule.model.StatutVehicule;

/**
 * Changement de statut (utilisé notamment par location-service au départ / retour).
 * Le kilométrage est facultatif : il est mis à jour lors du retour du véhicule.
 */
public record StatutUpdateRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutVehicule statut,

        @PositiveOrZero
        Integer kilometrage) {
}
