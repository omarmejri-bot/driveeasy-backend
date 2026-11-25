package tn.driveeasy.common;

import org.springframework.http.HttpStatus;

/** Ressource introuvable : HTTP 404. */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String ressource, Object id) {
        this(ressource + " introuvable (id = " + id + ")");
    }
}
