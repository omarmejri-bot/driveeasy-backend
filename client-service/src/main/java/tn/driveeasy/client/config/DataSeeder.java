package tn.driveeasy.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.driveeasy.client.model.CategorieClient;
import tn.driveeasy.client.model.Client;
import tn.driveeasy.client.repository.ClientRepository;

import java.time.LocalDate;

/**
 * Jeu de données initial (dates calculées par rapport à aujourd'hui pour rester valides).
 * Cas de test inclus :
 * <ul>
 *   <li>client 1 : 4 locations terminées -> passe FIDELE au prochain retour (démo) ;</li>
 *   <li>client 4 : 19 ans -> non éligible (âge) ;</li>
 *   <li>client 5 : permis expiré -> non éligible.</li>
 * </ul>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ClientRepository repository;
    private final boolean seedEnabled;

    public DataSeeder(ClientRepository repository,
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
        add("08123456", "Ben Ali", "Ahmed", LocalDate.of(1985, 3, 14), "ahmed.benali@mail.tn", "+216 22 123 456", "12 Rue de Marseille, Tunis", "TN-PC-100231", today.plusYears(4), CategorieClient.STANDARD, today.minusYears(2));
        add("09234567", "Trabelsi", "Fatma", LocalDate.of(1990, 7, 2), "fatma.trabelsi@mail.tn", "+216 98 234 567", "5 Avenue Habib Bourguiba, Sousse", "TN-PC-200452", today.plusYears(2), CategorieClient.FIDELE, today.minusYears(3));
        add("07345678", "Gharbi", "Mohamed", LocalDate.of(1978, 11, 20), "mohamed.gharbi@mail.tn", "+216 55 345 678", "Cité El Khadra, Tunis", "TN-PC-300873", today.plusYears(6), CategorieClient.VIP, today.minusYears(5));
        add("14456789", "Jebali", "Sami", today.minusYears(19).minusMonths(2), "sami.jebali@mail.tn", "+216 29 456 789", "Rue Ibn Khaldoun, Sfax", "TN-PC-401194", today.plusYears(9), CategorieClient.STANDARD, today.minusMonths(1));
        add("10567890", "Mansour", "Ines", LocalDate.of(1995, 5, 9), "ines.mansour@mail.tn", "+216 50 567 890", "Lac 2, Tunis", "TN-PC-501315", today.minusMonths(3), CategorieClient.STANDARD, today.minusYears(1));
        add("11678901", "Hammami", "Youssef", LocalDate.of(1988, 1, 30), "youssef.hammami@mail.tn", "+216 21 678 901", "Route de la Marsa, La Marsa", "TN-PC-601436", today.plusYears(3), CategorieClient.STANDARD, today.minusMonths(8));
        add("12789012", "Bouazizi", "Mariem", LocalDate.of(1999, 9, 17), "mariem.bouazizi@mail.tn", "+216 92 789 012", "Rue de Rome, Bizerte", "TN-PC-701557", today.plusYears(5), CategorieClient.STANDARD, today.minusMonths(4));
        add("06890123", "Chaouch", "Karim", LocalDate.of(1975, 12, 5), "karim.chaouch@mail.tn", "+216 58 890 123", "Les Berges du Lac, Tunis", "TN-PC-801678", today.plusYears(1), CategorieClient.VIP, today.minusYears(6));
        log.info("{} clients chargés en mémoire", repository.count());
    }

    private void add(String cin, String nom, String prenom, LocalDate naissance, String email, String tel,
                     String adresse, String permis, LocalDate expPermis, CategorieClient cat, LocalDate inscription) {
        Client c = new Client();
        c.setCin(cin);
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setDateNaissance(naissance);
        c.setEmail(email);
        c.setTelephone(tel);
        c.setAdresse(adresse);
        c.setNumeroPermis(permis);
        c.setDateExpirationPermis(expPermis);
        c.setCategorieClient(cat);
        c.setDateInscription(inscription);
        repository.save(c);
    }
}
