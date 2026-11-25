package tn.driveeasy.location.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Objets échangés lors des appels inter-services vers client-service et vehicule-service.
 */
public final class RemoteDtos {

    private RemoteDtos() {
    }

    public record ClientSummary(
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
            String categorieClient,
            LocalDate dateInscription) {
    }

    public record EligibiliteDto(
            Long clientId,
            String nomComplet,
            boolean eligible,
            int age,
            boolean permisValide,
            String categorieClient,
            List<String> motifs) {
    }

    public record CategorieUpdateDto(String categorieClient) {
    }

    public record VehiculeSummary(
            Long id,
            String immatriculation,
            String marque,
            String modele,
            int annee,
            String couleur,
            String carburant,
            String boite,
            int nbPlaces,
            int kilometrage,
            BigDecimal prixJournalier,
            String statut,
            String categorie,
            String imageUrl) {
    }

    public record StatutVehiculeUpdateDto(String statut, Integer kilometrage) {
    }
}
