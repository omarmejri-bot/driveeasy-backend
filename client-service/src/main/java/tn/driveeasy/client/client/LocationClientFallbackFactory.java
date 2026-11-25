package tn.driveeasy.client.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.driveeasy.common.RemoteCallSupport;

/**
 * Fallback : si location-service ne répond pas, on refuse la suppression (503)
 * plutôt que de risquer de supprimer un client ayant une location active.
 */
@Component
public class LocationClientFallbackFactory implements FallbackFactory<LocationClient> {

    @Override
    public LocationClient create(Throwable cause) {
        return clientId -> {
            throw RemoteCallSupport.translate("location-service", cause);
        };
    }
}
