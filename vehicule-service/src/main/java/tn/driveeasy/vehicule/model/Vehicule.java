package tn.driveeasy.vehicule.model;

import tn.driveeasy.common.Identifiable;

import java.math.BigDecimal;

/**
 * Entité Véhicule (stockée en mémoire). N'est jamais exposée directement :
 * les contrôleurs échangent des DTO (VehiculeRequest / VehiculeResponse).
 */
public class Vehicule implements Identifiable {

    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private int annee;
    private String couleur;
    private Carburant carburant;
    private Boite boite;
    private int nbPlaces;
    private int kilometrage;
    private BigDecimal prixJournalier;
    private StatutVehicule statut;
    private CategorieVehicule categorie;
    private String imageUrl;

    @Override
    public Long getId() { return id; }

    @Override
    public void setId(Long id) { this.id = id; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public Carburant getCarburant() { return carburant; }
    public void setCarburant(Carburant carburant) { this.carburant = carburant; }

    public Boite getBoite() { return boite; }
    public void setBoite(Boite boite) { this.boite = boite; }

    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }

    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }

    public BigDecimal getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(BigDecimal prixJournalier) { this.prixJournalier = prixJournalier; }

    public StatutVehicule getStatut() { return statut; }
    public void setStatut(StatutVehicule statut) { this.statut = statut; }

    public CategorieVehicule getCategorie() { return categorie; }
    public void setCategorie(CategorieVehicule categorie) { this.categorie = categorie; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
