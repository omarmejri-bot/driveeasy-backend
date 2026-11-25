package tn.driveeasy.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Traduit les réponses d'erreur HTTP des autres microservices (appels Feign)
 * en exceptions métier locales, en conservant le message d'origine.
 * Ex : un 404 de client-service devient une ResourceNotFoundException dans location-service.
 */
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = extractMessage(response);
        return switch (response.status()) {
            case 400 -> new BusinessRuleException(message);
            case 404 -> new ResourceNotFoundException(message);
            case 409 -> new ConflictException(message);
            default -> new ServiceUnavailableException(
                    "Erreur du service distant (" + methodKey + ", HTTP " + response.status() + ") : " + message);
        };
    }

    /** Lit le champ "message" du corps JSON d'erreur (format ErrorResponse). */
    private String extractMessage(Response response) {
        if (response.body() == null) {
            return "Erreur HTTP " + response.status();
        }
        try (InputStream is = response.body().asInputStream()) {
            JsonNode node = objectMapper.readTree(is);
            if (node != null && node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // corps non JSON : on garde un message générique
        }
        return "Erreur HTTP " + response.status();
    }
}
