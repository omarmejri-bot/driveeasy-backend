package tn.driveeasy.location.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Résultat d'une simulation tarifaire détaillée (sans création de réservation). */
public record SimulationResponse(
        Long clientId,
        String clientNomComplet,
        String categorieClient,
        boolean clientEligible,
        Long vehiculeId,
        String vehiculeLibelle,
        String categorieVehicule,
        boolean vehiculeDisponible,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        long nbJours,
        BigDecimal prixJournalierBase,
        BigDecimal coefficientCategorie,
        BigDecimal prixJournalierApplique,
        BigDecimal montantBrut,
        BigDecimal tauxRemiseFidelite,
        BigDecimal montantRemise,
        BigDecimal montantTotal,
        BigDecimal penaliteParJourRetard) {
}
