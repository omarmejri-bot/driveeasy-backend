package tn.driveeasy.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * client-service : gestion des clients, de leur identité et de la validité du permis.
 */
@SpringBootApplication(scanBasePackages = "tn.driveeasy")
@EnableFeignClients(basePackages = "tn.driveeasy")
public class ClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientServiceApplication.class, args);
    }
}
