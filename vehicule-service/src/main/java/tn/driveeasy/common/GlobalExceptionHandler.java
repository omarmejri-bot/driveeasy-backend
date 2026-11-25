package tn.driveeasy.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Gestionnaire centralisé des exceptions : transforme chaque exception
 * en réponse HTTP cohérente (codes 400, 404, 409, 500, 503) au format {@link ErrorResponse}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Erreurs métier (404, 409, 400) levées par la couche service. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getMessage(), req, null);
    }

    /** Service distant indisponible (circuit breaker / timeout). */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUnavailable(ServiceUnavailableException ex, HttpServletRequest req) {
        log.warn("Service distant indisponible : {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null);
    }

    /** Échec de validation @Valid sur le corps de la requête. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Données invalides", req, details);
    }

    /** Échec de validation sur les paramètres (@RequestParam, @PathVariable). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Paramètres invalides", req, details);
    }

    /** JSON mal formé ou valeur d'enum inconnue. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Corps de requête illisible ou valeur inconnue", req, null);
    }

    /** Paramètre de type incorrect (ex : id non numérique, date mal formatée). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Valeur invalide pour le paramètre '" + ex.getName() + "' : " + ex.getValue(), req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Paramètre obligatoire manquant : " + ex.getParameterName(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Ressource inexistante", req, null);
    }

    /** Toute autre erreur imprévue : HTTP 500 (le détail est journalisé, pas exposé). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erreur interne inattendue", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req, List<String> details) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
