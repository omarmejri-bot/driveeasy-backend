package tn.driveeasy.location.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.driveeasy.common.RemoteCallSupport;
import tn.driveeasy.location.dto.RemoteDtos.StatutVehiculeUpdateDto;
import tn.driveeasy.location.dto.RemoteDtos.VehiculeSummary;

import java.util.List;

/** Fallback Resilience4j pour vehicule-service. */
@Component
public class VehiculeServiceFallbackFactory implements FallbackFactory<VehiculeServiceClient> {

    @Override
    public VehiculeServiceClient create(Throwable cause) {
        return new VehiculeServiceClient() {
            @Override
            public List<VehiculeSummary> listerTous() {
                throw RemoteCallSupport.translate("vehicule-service", cause);
            }

            @Override
            public VehiculeSummary getVehicule(Long id) {
                throw RemoteCallSupport.translate("vehicule-service", cause);
            }

            @Override
            public VehiculeSummary changerStatut(Long id, StatutVehiculeUpdateDto body) {
                throw RemoteCallSupport.translate("vehicule-service", cause);
            }
        };
    }
}
