package tn.driveeasy.location.service;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import tn.driveeasy.common.BusinessRuleException;
import tn.driveeasy.location.config.TarificationProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calculateur tarifaire conforme aux règles de gestion du cahier des charges (§5.3) :
 * <ul>
 *   <li>{@code Prix = nbJours × prixJournalier × coefficientCatégorie − remiseFidélité}
 *       (FIDELE : 5 %, VIP : 10 %) ;</li>
 *   <li>{@code Pénalité de retard = 1,5 × prixJournalier × nbJoursRetard}.</li>
 * </ul>
 * Tous les calculs monétaires utilisent {@link BigDecimal} arrondi à 2 décimales (HALF_UP).
 */
@Service
@RefreshScope
public class TarificationService {

    private final TarificationProperties props;

    public TarificationService(TarificationProperties props) {
        this.props = props;
    }

    public record DevisTarifaire(
            long nbJours,
            BigDecimal prixJournalierBase,
            BigDecimal coefficientCategorie,
            BigDecimal prixJournalierApplique,
            BigDecimal montantBrut,
            BigDecimal tauxRemise,
            BigDecimal montantRemise,
            BigDecimal montantTotal,
            BigDecimal penaliteParJourRetard) {
    }

    /**
     * Nombre de jours facturés : au minimum 1 jour (location sur la même journée = 1 jour).
     */
    public static long calculerNombreJours(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new BusinessRuleException("La date de fin prévue (" + fin
                    + ") ne peut pas être antérieure à la date de début (" + debut + ")");
        }
        return Math.max(1L, ChronoUnit.DAYS.between(debut, fin));
    }

    public DevisTarifaire calculerDevis(LocalDate debut, LocalDate fin,
                                        BigDecimal prixJournalierBase,
                                        String categorieVehicule,
                                        String categorieClient) {
        long nbJours = calculerNombreJours(debut, fin);
        BigDecimal coef = props.coefficientPour(categorieVehicule);
        BigDecimal prixApplique = scale(prixJournalierBase.multiply(coef));
        BigDecimal brut = scale(prixApplique.multiply(BigDecimal.valueOf(nbJours)));

        BigDecimal tauxRemise = tauxRemisePour(categorieClient);
        BigDecimal remise = scale(brut.multiply(tauxRemise));
        BigDecimal net = scale(brut.subtract(remise));
        BigDecimal penaliteJour = scale(prixJournalierBase.multiply(props.getCoefficientPenalite()));

        return new DevisTarifaire(nbJours, scale(prixJournalierBase), coef,
                prixApplique, brut, tauxRemise, remise, net, penaliteJour);
    }

    /**
     * Pénalité de retard = 1,5 × prixJournalier × joursRetard (0 si retour à l'heure ou en avance).
     */
    public BigDecimal calculerPenalite(LocalDate dateFinPrevue, LocalDate dateRetourEffectif,
                                       BigDecimal prixJournalierBase) {
        if (dateRetourEffectif == null || !dateRetourEffectif.isAfter(dateFinPrevue)) {
            return scale(BigDecimal.ZERO);
        }
        long joursRetard = ChronoUnit.DAYS.between(dateFinPrevue, dateRetourEffectif);
        return scale(prixJournalierBase
                .multiply(props.getCoefficientPenalite())
                .multiply(BigDecimal.valueOf(joursRetard)));
    }

    public BigDecimal tauxRemisePour(String categorieClient) {
        if (categorieClient == null) {
            return BigDecimal.ZERO;
        }
        return switch (categorieClient.toUpperCase()) {
            case "FIDELE" -> props.getRemiseFidele();
            case "VIP" -> props.getRemiseVip();
            default -> BigDecimal.ZERO;
        };
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
