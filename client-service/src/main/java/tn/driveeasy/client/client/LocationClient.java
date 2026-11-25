package tn.driveeasy.client.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign vers location-service : permet de savoir si un client
 * a des locations actives (RESERVEE / EN_COURS) avant sa suppression.
 */
@FeignClient(name = "location-service", fallbackFactory = LocationClientFallbackFactory.class)
public interface LocationClient {

    @GetMapping("/locations/clients/{clientId}/actives/count")
    Long countLocationsActives(@PathVariable("clientId") Long clientId);
}
