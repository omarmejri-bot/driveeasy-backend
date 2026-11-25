package tn.driveeasy.common;

import org.springframework.http.HttpStatus;

/**
 * Un service distant est indisponible (panne, timeout, circuit ouvert) : HTTP 503.
 * Hérite de RuntimeException (et non d'ApiException) afin d'être comptée comme
 * une panne par le circuit breaker Resilience4j.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpStatus getStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}
