package tn.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Requête de création ou de simulation d'une réservation. */
public record LocationRequest(
        @NotNull(message = "L'identifiant du client est obligatoire")
        Long clientId,

        @NotNull(message = "L'identifiant du véhicule est obligatoire")
        Long vehiculeId,

        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,

        @NotNull(message = "La date de fin prévue est obligatoire")
        LocalDate dateFinPrevue) {
}
