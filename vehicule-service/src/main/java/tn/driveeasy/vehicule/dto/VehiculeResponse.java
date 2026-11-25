package tn.driveeasy.vehicule.dto;

import tn.driveeasy.vehicule.model.Boite;
import tn.driveeasy.vehicule.model.Carburant;
import tn.driveeasy.vehicule.model.CategorieVehicule;
import tn.driveeasy.vehicule.model.StatutVehicule;

import java.math.BigDecimal;

/** Représentation d'un véhicule renvoyée par l'API. */
public record VehiculeResponse(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        int annee,
        String couleur,
        Carburant carburant,
        Boite boite,
        int nbPlaces,
        int kilometrage,
        BigDecimal prixJournalier,
        StatutVehicule statut,
        CategorieVehicule categorie,
        String imageUrl) {
}
