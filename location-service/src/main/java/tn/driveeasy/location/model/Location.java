package tn.driveeasy.location.model;

import tn.driveeasy.common.Identifiable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Location (stockée en mémoire).
 * <p>
 * Les tarifs sont FIGÉS à la réservation (prixJournalierApplique, coefficient, remise) :
 * une modification ultérieure du prix du véhicule n'affecte pas les locations existantes.
 * Le nom du client et le libellé du véhicule sont dupliqués (snapshot) pour l'affichage
 * et les statistiques, sans dépendre de la disponibilité des autres services.
 */
public class Location implements Identifiable {

    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private String clientNomComplet;
    private String vehiculeLibelle;
    private String vehiculeImmatriculation;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private Integer kmDepart;
    private Integer kmRetour;
    private BigDecimal prixJournalierApplique;
    private BigDecimal coefficientCategorie;
    private BigDecimal tauxRemise;
    private BigDecimal montantRemise;
    private BigDecimal montantTotal;
    private BigDecimal penalite = BigDecimal.ZERO;
    private StatutLocation statut;
    private LocalDateTime dateCreation;

    @Override
    public Long getId() { return id; }

    @Override
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public String getClientNomComplet() { return clientNomComplet; }
    public void setClientNomComplet(String clientNomComplet) { this.clientNomComplet = clientNomComplet; }

    public String getVehiculeLibelle() { return vehiculeLibelle; }
    public void setVehiculeLibelle(String vehiculeLibelle) { this.vehiculeLibelle = vehiculeLibelle; }

    public String getVehiculeImmatriculation() { return vehiculeImmatriculation; }
    public void setVehiculeImmatriculation(String vehiculeImmatriculation) { this.vehiculeImmatriculation = vehiculeImmatriculation; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate dateRetourEffectif) { this.dateRetourEffectif = dateRetourEffectif; }

    public Integer getKmDepart() { return kmDepart; }
    public void setKmDepart(Integer kmDepart) { this.kmDepart = kmDepart; }

    public Integer getKmRetour() { return kmRetour; }
    public void setKmRetour(Integer kmRetour) { this.kmRetour = kmRetour; }

    public BigDecimal getPrixJournalierApplique() { return prixJournalierApplique; }
    public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) { this.prixJournalierApplique = prixJournalierApplique; }

    public BigDecimal getCoefficientCategorie() { return coefficientCategorie; }
    public void setCoefficientCategorie(BigDecimal coefficientCategorie) { this.coefficientCategorie = coefficientCategorie; }

    public BigDecimal getTauxRemise() { return tauxRemise; }
    public void setTauxRemise(BigDecimal tauxRemise) { this.tauxRemise = tauxRemise; }

    public BigDecimal getMontantRemise() { return montantRemise; }
    public void setMontantRemise(BigDecimal montantRemise) { this.montantRemise = montantRemise; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public BigDecimal getPenalite() { return penalite; }
    public void setPenalite(BigDecimal penalite) { this.penalite = penalite; }

    public StatutLocation getStatut() { return statut; }
    public void setStatut(StatutLocation statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    /** Montant final à payer = prix de la location + pénalités de retard. */
    public BigDecimal getMontantAPayer() {
        return montantTotal.add(penalite == null ? BigDecimal.ZERO : penalite);
    }

    /** Une location "active" bloque le véhicule et empêche la suppression du client. */
    public boolean isActive() {
        return statut == StatutLocation.RESERVEE || statut == StatutLocation.EN_COURS;
    }
}
