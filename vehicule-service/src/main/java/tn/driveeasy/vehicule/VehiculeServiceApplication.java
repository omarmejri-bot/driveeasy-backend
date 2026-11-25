package tn.driveeasy.vehicule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * vehicule-service : gestion du parc automobile (CRUD, statut, disponibilité).
 * scanBasePackages = "tn.driveeasy" pour inclure le package commun (exceptions, dépôt générique).
 */
@SpringBootApplication(scanBasePackages = "tn.driveeasy")
@EnableFeignClients(basePackages = "tn.driveeasy")
public class VehiculeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiculeServiceApplication.class, args);
    }
}
