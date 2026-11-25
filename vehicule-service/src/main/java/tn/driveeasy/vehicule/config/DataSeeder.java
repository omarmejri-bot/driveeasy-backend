package tn.driveeasy.vehicule.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.driveeasy.vehicule.model.Boite;
import tn.driveeasy.vehicule.model.Carburant;
import tn.driveeasy.vehicule.model.CategorieVehicule;
import tn.driveeasy.vehicule.model.StatutVehicule;
import tn.driveeasy.vehicule.model.Vehicule;
import tn.driveeasy.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;

/**
 * Jeu de données initial chargé en RAM au démarrage (activable via driveeasy.store.seed-enabled).
 * Les véhicules 2 et 4 sont LOUE car ils correspondent aux locations EN_COURS de location-service ;
 * le véhicule 8 est en MAINTENANCE.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final VehiculeRepository repository;
    private final boolean seedEnabled;

    public DataSeeder(VehiculeRepository repository,
                      @Value("${driveeasy.store.seed-enabled:true}") boolean seedEnabled) {
        this.repository = repository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || repository.count() > 0) {
            return;
        }
        add("201 TU 1234", "Peugeot", "208", 2023, "Blanc", Carburant.ESSENCE, Boite.MANUELLE, 5, 18500, "90", StatutVehicule.DISPONIBLE, CategorieVehicule.COMPACTE, "peugeot", "208");
        add("198 TU 5678", "Renault", "Clio", 2022, "Rouge", Carburant.DIESEL, Boite.MANUELLE, 5, 42000, "75", StatutVehicule.LOUE, CategorieVehicule.ECONOMIQUE, "renault", "clio");
        add("215 TU 9012", "Volkswagen", "Golf", 2024, "Gris", Carburant.ESSENCE, Boite.AUTOMATIQUE, 5, 8000, "120", StatutVehicule.DISPONIBLE, CategorieVehicule.COMPACTE, "volkswagen", "golf");
        add("210 TU 3456", "Kia", "Sportage", 2023, "Noir", Carburant.DIESEL, Boite.AUTOMATIQUE, 5, 25000, "180", StatutVehicule.LOUE, CategorieVehicule.SUV, "kia", "sportage");
        add("220 TU 7890", "Toyota", "Corolla", 2024, "Bleu", Carburant.HYBRIDE, Boite.AUTOMATIQUE, 5, 12000, "140", StatutVehicule.DISPONIBLE, CategorieVehicule.BERLINE, "toyota", "corolla");
        add("190 TU 2468", "Dacia", "Sandero", 2021, "Blanc", Carburant.ESSENCE, Boite.MANUELLE, 5, 65000, "60", StatutVehicule.DISPONIBLE, CategorieVehicule.ECONOMIQUE, "dacia", "sandero");
        add("222 TU 1357", "Mercedes-Benz", "Classe C", 2025, "Noir", Carburant.DIESEL, Boite.AUTOMATIQUE, 5, 5000, "320", StatutVehicule.DISPONIBLE, CategorieVehicule.LUXE, "mercedes-benz", "c-class");
        add("205 TU 8642", "Hyundai", "Tucson", 2022, "Gris", Carburant.HYBRIDE, Boite.AUTOMATIQUE, 5, 38000, "170", StatutVehicule.MAINTENANCE, CategorieVehicule.SUV, "hyundai", "tucson");
        add("218 TU 9753", "Tesla", "Model 3", 2024, "Blanc", Carburant.ELECTRIQUE, Boite.AUTOMATIQUE, 5, 15000, "280", StatutVehicule.DISPONIBLE, CategorieVehicule.LUXE, "tesla", "model-3");
        add("200 TU 1122", "Fiat", "500", 2022, "Jaune", Carburant.ESSENCE, Boite.MANUELLE, 4, 30000, "65", StatutVehicule.DISPONIBLE, CategorieVehicule.ECONOMIQUE, "fiat", "500");
        add("212 TU 3344", "Skoda", "Octavia", 2023, "Vert", Carburant.DIESEL, Boite.MANUELLE, 5, 27000, "130", StatutVehicule.DISPONIBLE, CategorieVehicule.BERLINE, "skoda", "octavia");
        add("225 TU 5566", "BMW", "X5", 2025, "Bleu nuit", Carburant.DIESEL, Boite.AUTOMATIQUE, 7, 3000, "450", StatutVehicule.DISPONIBLE, CategorieVehicule.LUXE, "bmw", "x5");
        log.info("{} véhicules chargés en mémoire", repository.count());
    }

    private void add(String immat, String marque, String modele, int annee, String couleur, Carburant carburant,
                     Boite boite, int places, int km, String prix, StatutVehicule statut,
                     CategorieVehicule categorie, String make, String family) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immat);
        v.setMarque(marque);
        v.setModele(modele);
        v.setAnnee(annee);
        v.setCouleur(couleur);
        v.setCarburant(carburant);
        v.setBoite(boite);
        v.setNbPlaces(places);
        v.setKilometrage(km);
        v.setPrixJournalier(new BigDecimal(prix));
        v.setStatut(statut);
        v.setCategorie(categorie);
        v.setImageUrl("https://cdn.imagin.studio/getImage?customer=img&make=" + make + "&modelFamily=" + family + "&angle=23");
        repository.save(v);
    }
}
