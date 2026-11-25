package tn.driveeasy.client.dto;

import tn.driveeasy.client.model.CategorieClient;

import java.time.LocalDate;

/** Représentation d'un client renvoyée par l'API (avec champs calculés : âge, validité du permis). */
public record ClientResponse(
        Long id,
        String cin,
        String nom,
        String prenom,
        LocalDate dateNaissance,
        int age,
        String email,
        String telephone,
        String adresse,
        String numeroPermis,
        LocalDate dateExpirationPermis,
        boolean permisValide,
        CategorieClient categorieClient,
        LocalDate dateInscription) {
}
