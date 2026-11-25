package tn.driveeasy.vehicule.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.driveeasy.common.RemoteCallSupport;

import java.time.LocalDate;
import java.util.List;

/**
 * Fallback (Resilience4j) : si location-service est indisponible, on NE renvoie PAS
 * une liste vide (cela laisserait croire que tous les véhicules sont libres) :
 * on lève une erreur 503 explicite.
 */
@Component
public class LocationClientFallbackFactory implements FallbackFactory<LocationClient> {

    @Override
    public LocationClient create(Throwable cause) {
        return new LocationClient() {
            @Override
            public List<Long> getVehiculesOccupes(LocalDate dateDebut, LocalDate dateFin) {
                throw RemoteCallSupport.translate("location-service", cause);
            }
        };
    }
}
