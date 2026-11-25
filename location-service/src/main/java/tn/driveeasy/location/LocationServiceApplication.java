package tn.driveeasy.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * location-service : cycle de vie des locations (réservation, départ, retour, annulation)
 * et orchestration des appels vers client-service et vehicule-service.
 */
@SpringBootApplication(scanBasePackages = "tn.driveeasy")
@EnableFeignClients(basePackages = "tn.driveeasy")
@ConfigurationPropertiesScan("tn.driveeasy")
public class LocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }
}
