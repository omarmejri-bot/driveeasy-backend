package tn.driveeasy.location.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import tn.driveeasy.location.dto.RemoteDtos.CategorieUpdateDto;
import tn.driveeasy.location.dto.RemoteDtos.ClientSummary;
import tn.driveeasy.location.dto.RemoteDtos.EligibiliteDto;

/**
 * Communication inter-services location-service -> client-service (via Eureka).
 * Contrôle de l'éligibilité, lecture du client et mise à jour de la catégorie de fidélité.
 */
@FeignClient(name = "client-service", fallbackFactory = ClientServiceFallbackFactory.class)
public interface ClientServiceClient {

    @GetMapping("/clients/{id}")
    ClientSummary getClient(@PathVariable("id") Long id);

    @GetMapping("/clients/{id}/eligibilite")
    EligibiliteDto verifierEligibilite(@PathVariable("id") Long id);

    @PatchMapping("/clients/{id}/categorie")
    ClientSummary changerCategorie(@PathVariable("id") Long id, @RequestBody CategorieUpdateDto body);
}
