package tn.driveeasy.common;

/**
 * Utilitaire pour les fallbacks Feign / Resilience4j.
 * <p>
 * Le circuit breaker appelle le fallback pour TOUTE exception, y compris les erreurs
 * métier (404, 409...). On distingue donc :
 * <ul>
 *   <li>erreur métier ({@link ApiException}) : propagée telle quelle ;</li>
 *   <li>panne technique (timeout, service arrêté, circuit ouvert) : 503.</li>
 * </ul>
 */
public final class RemoteCallSupport {

    private RemoteCallSupport() {
    }

    public static RuntimeException translate(String serviceName, Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof ApiException apiException) {
                return apiException;
            }
            if (current instanceof ServiceUnavailableException sue) {
                return sue;
            }
            current = current.getCause();
        }
        return new ServiceUnavailableException(
                "Le service " + serviceName + " est momentanément indisponible. Réessayez plus tard.", cause);
    }
}
