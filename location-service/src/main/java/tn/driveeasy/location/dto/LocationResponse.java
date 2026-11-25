package tn.driveeasy.location.dto;

import tn.driveeasy.location.model.StatutLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Représentation d'une location dans les listes et après création / mise à jour. */
public record LocationResponse(
        Long id,
        Long clientId,
        String clientNomComplet,
        Long vehiculeId,
        String vehiculeLibelle,
        String vehiculeImmatriculation,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        LocalDate dateRetourEffectif,
        long nbJoursPrevus,
        long joursRetard,
        Integer kmDepart,
        Integer kmRetour,
        Integer distanceParcourue,
        BigDecimal prixJournalierApplique,
        BigDecimal coefficientCategorie,
        BigDecimal tauxRemise,
        BigDecimal montantRemise,
        BigDecimal montantTotal,
        BigDecimal penalite,
        BigDecimal montantAPayer,
        StatutLocation statut,
        LocalDateTime dateCreation) {
}
