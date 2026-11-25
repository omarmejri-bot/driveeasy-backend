package tn.driveeasy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway : point d'entrée unique du système.
 * Les routes, le CORS et le load balancing sont définis dans config-repo/api-gateway.yml.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
