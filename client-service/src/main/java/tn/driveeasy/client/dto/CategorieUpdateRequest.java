package tn.driveeasy.client.dto;

import jakarta.validation.constraints.NotNull;
import tn.driveeasy.client.model.CategorieClient;

/** Mise à jour de la catégorie de fidélité (pilotée par location-service). */
public record CategorieUpdateRequest(
        @NotNull(message = "La catégorie est obligatoire")
        CategorieClient categorieClient) {
}
