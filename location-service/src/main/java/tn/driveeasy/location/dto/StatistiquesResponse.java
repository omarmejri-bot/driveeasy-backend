package tn.driveeasy.location.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Indicateurs métier pour le tableau de bord (GET /locations/statistiques). */
public record StatistiquesResponse(
        BigDecimal chiffreAffairesTotal,
        BigDecimal chiffreAffairesMoisCourant,
        BigDecimal totalPenalites,
        double tauxOccupationParc,
        long totalVehicules,
        long vehiculesDisponibles,
        long vehiculesLoues,
        long vehiculesMaintenance,
        long totalLocations,
        long locationsEnCours,
        long locationsReservees,
        long locationsTerminees,
        long locationsAnnulees,
        Map<String, Long> repartitionParStatut,
        List<TopVehiculeStat> topVehicules) {

    public record TopVehiculeStat(
            Long vehiculeId,
            String libelle,
            String immatriculation,
            long nombreLocations,
            BigDecimal chiffreAffairesGenere) {
    }
}
