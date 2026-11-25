package tn.driveeasy.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tn.driveeasy.client.model.CategorieClient;

import java.time.LocalDate;

/** Données reçues pour créer / modifier un client. */
public record ClientRequest(
        @NotBlank(message = "Le CIN est obligatoire")
        @Pattern(regexp = "^\\d{8}$", message = "Le CIN doit contenir exactement 8 chiffres")
        String cin,

        @NotBlank(message = "Le nom est obligatoire") @Size(max = 50)
        String nom,

        @NotBlank(message = "Le prénom est obligatoire") @Size(max = 50)
        String prenom,

        @NotNull(message = "La date de naissance est obligatoire")
        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String email,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9 ]{8,15}$", message = "Numéro de téléphone invalide")
        String telephone,

        @Size(max = 200)
        String adresse,

        @NotBlank(message = "Le numéro de permis est obligatoire") @Size(max = 30)
        String numeroPermis,

        @NotNull(message = "La date d'expiration du permis est obligatoire")
        LocalDate dateExpirationPermis,

        /* Facultatif : STANDARD par défaut */
        CategorieClient categorieClient) {
}
