package tn.driveeasy.common;

import org.springframework.http.HttpStatus;

/**
 * Exception métier de base : porte le code HTTP à renvoyer au client.
 * Toutes les erreurs fonctionnelles (404, 409, 400, 503) en héritent.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
