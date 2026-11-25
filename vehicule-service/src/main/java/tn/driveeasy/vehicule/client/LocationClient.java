package tn.driveeasy.vehicule.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Client Feign vers location-service (résolu via Eureka : "location-service").
 * Sert à connaître les véhicules déjà réservés / loués sur une période.
 */
@FeignClient(name = "location-service", fallbackFactory = LocationClientFallbackFactory.class)
public interface LocationClient {

    @GetMapping("/locations/vehicules-occupes")
    List<Long> getVehiculesOccupes(
            @RequestParam("dateDebut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam("dateFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin);
}
