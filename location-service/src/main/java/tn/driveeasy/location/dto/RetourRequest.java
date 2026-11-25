package tn.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/** Retour du véhicule : saisie du kilométrage et date de retour (aujourd'hui par défaut). */
public record RetourRequest(
        @NotNull(message = "Le kilométrage de retour est obligatoire")
        @PositiveOrZero(message = "Le kilométrage doit être positif ou nul")
        Integer kmRetour,

        LocalDate dateRetourEffectif) {
}
