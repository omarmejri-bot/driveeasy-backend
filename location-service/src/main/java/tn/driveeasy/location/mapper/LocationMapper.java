package tn.driveeasy.location.mapper;

import tn.driveeasy.location.dto.LocationResponse;
import tn.driveeasy.location.model.Location;
import tn.driveeasy.location.service.TarificationService;

import java.time.temporal.ChronoUnit;

/** Conversion Location -> LocationResponse avec calcul des durées et distances. */
public final class LocationMapper {

    private LocationMapper() {
    }

    public static LocationResponse toResponse(Location l) {
        long nbJours = TarificationService.calculerNombreJours(l.getDateDebut(), l.getDateFinPrevue());
        long retard = 0;
        if (l.getDateRetourEffectif() != null && l.getDateRetourEffectif().isAfter(l.getDateFinPrevue())) {
            retard = ChronoUnit.DAYS.between(l.getDateFinPrevue(), l.getDateRetourEffectif());
        }
        Integer distance = (l.getKmDepart() != null && l.getKmRetour() != null)
                ? l.getKmRetour() - l.getKmDepart()
                : null;
        return new LocationResponse(
                l.getId(),
                l.getClientId(),
                l.getClientNomComplet(),
                l.getVehiculeId(),
                l.getVehiculeLibelle(),
                l.getVehiculeImmatriculation(),
                l.getDateDebut(),
                l.getDateFinPrevue(),
                l.getDateRetourEffectif(),
                nbJours,
                retard,
                l.getKmDepart(),
                l.getKmRetour(),
                distance,
                l.getPrixJournalierApplique(),
                l.getCoefficientCategorie(),
                l.getTauxRemise(),
                l.getMontantRemise(),
                l.getMontantTotal(),
                l.getPenalite(),
                l.getMontantAPayer(),
                l.getStatut(),
                l.getDateCreation());
    }
}
