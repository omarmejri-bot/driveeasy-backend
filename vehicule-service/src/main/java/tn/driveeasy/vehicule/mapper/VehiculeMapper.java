package tn.driveeasy.vehicule.mapper;

import tn.driveeasy.vehicule.dto.VehiculeRequest;
import tn.driveeasy.vehicule.dto.VehiculeResponse;
import tn.driveeasy.vehicule.model.StatutVehicule;
import tn.driveeasy.vehicule.model.Vehicule;

/** Conversion entité <-> DTO (séparation claire entre modèle interne et objets échangés). */
public final class VehiculeMapper {

    private VehiculeMapper() {
    }

    public static VehiculeResponse toResponse(Vehicule v) {
        return new VehiculeResponse(v.getId(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                v.getAnnee(), v.getCouleur(), v.getCarburant(), v.getBoite(), v.getNbPlaces(),
                v.getKilometrage(), v.getPrixJournalier(), v.getStatut(), v.getCategorie(), v.getImageUrl());
    }

    /** Crée une nouvelle entité à partir de la requête (statut DISPONIBLE par défaut). */
    public static Vehicule toEntity(VehiculeRequest r) {
        Vehicule v = new Vehicule();
        updateEntity(v, r);
        v.setStatut(r.statut() != null ? r.statut() : StatutVehicule.DISPONIBLE);
        return v;
    }

    /** Copie les champs modifiables (le statut est géré séparément par le service). */
    public static void updateEntity(Vehicule v, VehiculeRequest r) {
        v.setImmatriculation(normaliserImmatriculation(r.immatriculation()));
        v.setMarque(r.marque().trim());
        v.setModele(r.modele().trim());
        v.setAnnee(r.annee());
        v.setCouleur(r.couleur());
        v.setCarburant(r.carburant());
        v.setBoite(r.boite());
        v.setNbPlaces(r.nbPlaces());
        v.setKilometrage(r.kilometrage());
        v.setPrixJournalier(r.prixJournalier());
        v.setCategorie(r.categorie());
        v.setImageUrl(r.imageUrl());
    }

    /** "123 tu 4567" et "123 TU 4567" désignent la même plaque. */
    public static String normaliserImmatriculation(String immat) {
        return immat == null ? null : immat.trim().toUpperCase().replaceAll("\\s+", " ");
    }
}
