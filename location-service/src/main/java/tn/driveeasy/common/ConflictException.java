package tn.driveeasy.common;

import org.springframework.http.HttpStatus;

/** Conflit avec l'état actuel (doublon, ressource indisponible, transition interdite) : HTTP 409. */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
