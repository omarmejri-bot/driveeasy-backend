package tn.driveeasy.vehicule.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tn.driveeasy.vehicule.model.Boite;
import tn.driveeasy.vehicule.model.Carburant;
import tn.driveeasy.vehicule.model.CategorieVehicule;
import tn.driveeasy.vehicule.model.StatutVehicule;

import java.math.BigDecimal;

/**
 * Données reçues pour créer / modifier un véhicule (validées par Bean Validation).
 * La cohérence de l'année (≤ année courante) est vérifiée dans la couche service.
 */
public record VehiculeRequest(
        @NotBlank(message = "L'immatriculation est obligatoire")
        @Size(max = 20)
        @Pattern(regexp = "^[A-Za-z0-9 -]+$", message = "Format d'immatriculation invalide")
        String immatriculation,

        @NotBlank(message = "La marque est obligatoire") @Size(max = 50)
        String marque,

        @NotBlank(message = "Le modèle est obligatoire") @Size(max = 50)
        String modele,

        @NotNull(message = "L'année est obligatoire") @Min(value = 1990, message = "L'année doit être ≥ 1990")
        Integer annee,

        @Size(max = 30)
        String couleur,

        @NotNull(message = "Le carburant est obligatoire")
        Carburant carburant,

        @NotNull(message = "La boîte est obligatoire")
        Boite boite,

        @NotNull @Min(value = 2, message = "Au moins 2 places") @Max(value = 9, message = "Au plus 9 places")
        Integer nbPlaces,

        @NotNull @PositiveOrZero(message = "Le kilométrage doit être positif ou nul")
        Integer kilometrage,

        @NotNull(message = "Le prix journalier est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix journalier doit être strictement positif")
        @Digits(integer = 6, fraction = 2)
        BigDecimal prixJournalier,

        /* Facultatif : DISPONIBLE par défaut à la création */
        StatutVehicule statut,

        @NotNull(message = "La catégorie est obligatoire")
        CategorieVehicule categorie,

        @Size(max = 500)
        String imageUrl) {
}
