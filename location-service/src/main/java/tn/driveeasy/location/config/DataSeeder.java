package tn.driveeasy.location.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.driveeasy.location.model.Location;
import tn.driveeasy.location.model.StatutLocation;
import tn.driveeasy.location.repository.LocationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Jeu de données initial des locations :
 * <ul>
 *   <li>4 locations TERMINEE pour le client 1 (Ahmed Ben Ali, STANDARD)
 *       + 1 location EN_COURS (id=5, véhicule 2) -> lors de son retour en démo,
 *       Ahmed atteint 5 locations terminées et passe automatiquement en FIDELE !</li>
 *   <li>1 location EN_COURS (id=6, véhicule 4) dont la dateFinPrevue est dépassée de 2 jours
 *       -> permet de démontrer le calcul automatique des pénalités de retard ;</li>
 *   <li>1 location RESERVEE (id=7, véhicule 3) -> prête pour démontrer le départ ou l'annulation ;</li>
 *   <li>1 location ANNULEE (id=8).</li>
 * </ul>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final LocationRepository repository;
    private final boolean seedEnabled;

    public DataSeeder(LocationRepository repository,
                      @Value("${driveeasy.store.seed-enabled:true}") boolean seedEnabled) {
        this.repository = repository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || repository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();

        // 4 locations TERMINEE pour le client 1 (Ahmed Ben Ali) -> 5e retour = promotion FIDELE
        add(1L, "Ahmed Ben Ali", 1L, "Peugeot 208", "201 TU 1234",
                today.minusDays(60), today.minusDays(57), today.minusDays(57),
                15000, 15420, "90.00", "1.10", "0.00", "0.00", "297.00", "0.00", StatutLocation.TERMINEE);
        add(1L, "Ahmed Ben Ali", 5L, "Toyota Corolla", "220 TU 7890",
                today.minusDays(45), today.minusDays(42), today.minusDays(41),
                10000, 10650, "140.00", "1.20", "0.00", "0.00", "504.00", "210.00", StatutLocation.TERMINEE);
        add(1L, "Ahmed Ben Ali", 6L, "Dacia Sandero", "190 TU 2468",
                today.minusDays(30), today.minusDays(28), today.minusDays(28),
                63000, 63350, "60.00", "1.00", "0.00", "0.00", "120.00", "0.00", StatutLocation.TERMINEE);
        add(1L, "Ahmed Ben Ali", 3L, "Volkswagen Golf", "215 TU 9012",
                today.minusDays(15), today.minusDays(12), today.minusDays(12),
                7200, 7680, "120.00", "1.10", "0.00", "0.00", "396.00", "0.00", StatutLocation.TERMINEE);

        // Location 5 : EN_COURS pour client 1 sur véhicule 2 (Renault Clio, LOUE)
        add(1L, "Ahmed Ben Ali", 2L, "Renault Clio", "198 TU 5678",
                today.minusDays(3), today.plusDays(1), null,
                42000, null, "75.00", "1.00", "0.00", "0.00", "300.00", "0.00", StatutLocation.EN_COURS);

        // Location 6 : EN_COURS en retard de 2 jours pour client 2 (Fatma Trabelsi, FIDELE) sur véhicule 4 (Kia Sportage, LOUE)
        add(2L, "Fatma Trabelsi", 4L, "Kia Sportage", "210 TU 3456",
                today.minusDays(6), today.minusDays(2), null,
                25000, null, "180.00", "1.40", "0.05", "47.88", "957.60", "0.00", StatutLocation.EN_COURS);

        // Location 7 : RESERVEE à venir pour client 3 (Mohamed Gharbi, VIP) sur véhicule 7 (Mercedes Classe C)
        add(3L, "Mohamed Gharbi", 7L, "Mercedes-Benz Classe C", "222 TU 1357",
                today.plusDays(2), today.plusDays(5), null,
                5000, null, "320.00", "1.80", "0.10", "172.80", "1555.20", "0.00", StatutLocation.RESERVEE);

        // Location 8 : TERMINEE ce mois-ci pour alimenter le CA du mois courant
        add(3L, "Mohamed Gharbi", 9L, "Tesla Model 3", "218 TU 9753",
                today.withDayOfMonth(1), today.withDayOfMonth(1).plusDays(3), today.withDayOfMonth(1).plusDays(3),
                14200, 14890, "280.00", "1.80", "0.10", "151.20", "1360.80", "0.00", StatutLocation.TERMINEE);

        // Location 9 : ANNULEE
        add(6L, "Youssef Hammami", 10L, "Fiat 500", "200 TU 1122",
                today.minusDays(10), today.minusDays(8), null,
                30000, null, "65.00", "1.00", "0.00", "0.00", "130.00", "0.00", StatutLocation.ANNULEE);

        log.info("{} locations chargées en mémoire", repository.count());
    }

    private void add(Long clientId, String clientNom, Long vehiculeId, String vehiculeLib, String immat,
                     LocalDate debut, LocalDate finPrevue, LocalDate retourEffectif,
                     Integer kmDep, Integer kmRet,
                     String prixJour, String coef, String tauxRemise, String remise, String total, String penalite,
                     StatutLocation statut) {
        Location l = new Location();
        l.setClientId(clientId);
        l.setClientNomComplet(clientNom);
        l.setVehiculeId(vehiculeId);
        l.setVehiculeLibelle(vehiculeLib);
        l.setVehiculeImmatriculation(immat);
        l.setDateDebut(debut);
        l.setDateFinPrevue(finPrevue);
        l.setDateRetourEffectif(retourEffectif);
        l.setKmDepart(kmDep);
        l.setKmRetour(kmRet);
        l.setPrixJournalierApplique(new BigDecimal(prixJour));
        l.setCoefficientCategorie(new BigDecimal(coef));
        l.setTauxRemise(new BigDecimal(tauxRemise));
        l.setMontantRemise(new BigDecimal(remise));
        l.setMontantTotal(new BigDecimal(total));
        l.setPenalite(new BigDecimal(penalite));
        l.setStatut(statut);
        l.setDateCreation(LocalDateTime.now().minusDays(5));
        repository.save(l);
    }
}
