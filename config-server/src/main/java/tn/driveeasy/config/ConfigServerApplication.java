package tn.driveeasy.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server : expose la configuration de chaque service
 * (fichiers {service}.yml / {service}-{profil}.yml stockés dans un dépôt Git).
 * Exemple : http://localhost:8888/vehicule-service/dev
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
