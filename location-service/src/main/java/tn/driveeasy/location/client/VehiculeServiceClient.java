package tn.driveeasy.location.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import tn.driveeasy.location.dto.RemoteDtos.StatutVehiculeUpdateDto;
import tn.driveeasy.location.dto.RemoteDtos.VehiculeSummary;

import java.util.List;

/**
 * Communication inter-services location-service -> vehicule-service (via Eureka).
 * Récupération du tarif / statut et mise à jour du statut (LOUE au départ, DISPONIBLE au retour).
 */
@FeignClient(name = "vehicule-service", fallbackFactory = VehiculeServiceFallbackFactory.class)
public interface VehiculeServiceClient {

    @GetMapping("/vehicules")
    List<VehiculeSummary> listerTous();

    @GetMapping("/vehicules/{id}")
    VehiculeSummary getVehicule(@PathVariable("id") Long id);

    @PatchMapping("/vehicules/{id}/statut")
    VehiculeSummary changerStatut(@PathVariable("id") Long id, @RequestBody StatutVehiculeUpdateDto body);
}
