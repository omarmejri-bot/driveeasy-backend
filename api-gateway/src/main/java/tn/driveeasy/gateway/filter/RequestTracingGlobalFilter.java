package tn.driveeasy.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

/**
 * Filtre global personnalisé appliqué à TOUTES les routes :
 * <ul>
 *   <li>génère (ou propage) un identifiant de requête {@code X-Request-Id} ;</li>
 *   <li>mesure le temps de traitement et l'ajoute dans {@code X-Response-Time} ;</li>
 *   <li>indique l'instance qui a servi la requête dans {@code X-Served-By}
 *       (permet de visualiser le load balancing entre plusieurs instances) ;</li>
 *   <li>journalise méthode, chemin, statut et durée.</li>
 * </ul>
 */
@Component
public class RequestTracingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestTracingGlobalFilter.class);
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();

        // 1) Identifiant de requête : réutilisé s'il est fourni par le client, sinon généré
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;

        // On transmet l'identifiant au microservice en aval
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(REQUEST_ID_HEADER, finalRequestId)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // 2) Juste avant l'envoi de la réponse : ajout des en-têtes calculés
        mutatedExchange.getResponse().beforeCommit(() -> {
            long duration = System.currentTimeMillis() - start;
            var headers = mutatedExchange.getResponse().getHeaders();
            headers.set(REQUEST_ID_HEADER, finalRequestId);
            headers.set("X-Response-Time", duration + "ms");
            // URL réelle choisie par le load balancer (ex: http://192.168.1.10:8091/vehicules)
            URI target = mutatedExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            if (target != null) {
                headers.set("X-Served-By", target.getHost() + ":" + target.getPort());
            }
            return Mono.empty();
        });

        // 3) Journalisation une fois la requête terminée
        return chain.filter(mutatedExchange).doFinally(signal -> {
            long duration = System.currentTimeMillis() - start;
            log.info("[{}] {} {} -> {} ({} ms)",
                    finalRequestId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getRawPath(),
                    mutatedExchange.getResponse().getStatusCode(),
                    duration);
        });
    }

    /** Exécuté en premier (avant le routage et le load balancing). */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
