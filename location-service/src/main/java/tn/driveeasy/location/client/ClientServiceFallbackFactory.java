package tn.driveeasy.location.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.driveeasy.common.RemoteCallSupport;
import tn.driveeasy.location.dto.RemoteDtos.CategorieUpdateDto;
import tn.driveeasy.location.dto.RemoteDtos.ClientSummary;
import tn.driveeasy.location.dto.RemoteDtos.EligibiliteDto;

/** Fallback Resilience4j pour client-service. */
@Component
public class ClientServiceFallbackFactory implements FallbackFactory<ClientServiceClient> {

    @Override
    public ClientServiceClient create(Throwable cause) {
        return new ClientServiceClient() {
            @Override
            public ClientSummary getClient(Long id) {
                throw RemoteCallSupport.translate("client-service", cause);
            }

            @Override
            public EligibiliteDto verifierEligibilite(Long id) {
                throw RemoteCallSupport.translate("client-service", cause);
            }

            @Override
            public ClientSummary changerCategorie(Long id, CategorieUpdateDto body) {
                throw RemoteCallSupport.translate("client-service", cause);
            }
        };
    }
}
