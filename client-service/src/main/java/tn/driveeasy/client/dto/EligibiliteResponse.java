package tn.driveeasy.client.dto;

import tn.driveeasy.client.model.CategorieClient;

import java.util.List;

/**
 * Résultat du contrôle d'éligibilité (âge ≥ 21 ans ET permis non expiré).
 * "motifs" liste les raisons d'un refus.
 */
public record EligibiliteResponse(
        Long clientId,
        String nomComplet,
        boolean eligible,
        int age,
        boolean permisValide,
        CategorieClient categorieClient,
        List<String> motifs) {
}
