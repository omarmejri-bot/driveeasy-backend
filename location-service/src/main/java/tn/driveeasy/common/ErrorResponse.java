package tn.driveeasy.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Format UNIFORME des erreurs renvoyées par tous les microservices.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {

    public static ErrorResponse of(int status, String error, String message, String path, List<String> details) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, details);
    }
}
