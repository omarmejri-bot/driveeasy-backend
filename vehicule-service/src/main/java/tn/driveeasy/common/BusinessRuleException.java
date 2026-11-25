package tn.driveeasy.common;

import org.springframework.http.HttpStatus;

/** Règle métier non respectée par les données envoyées : HTTP 400. */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
