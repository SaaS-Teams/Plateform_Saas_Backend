package tg.univlome.saas.shared.exceptions;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import tg.univlome.saas.shared.response.ApiResponse;

/**
 * Gestionnaire global des exceptions pour toute l'application.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère toutes les exceptions héritant de BaseException.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex, WebRequest request) {
        log.error("Erreur métier : {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), null);
        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Gère les erreurs de validation des requêtes (ex: @Valid, @NotNull).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Erreur de validation de la requête : {}", errors);
        ApiResponse<Void> response = ApiResponse.error("Erreur de validation", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère toutes les autres exceptions non prévues (Erreur 500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Erreur inattendue : ", ex);
        ApiResponse<Void> response = ApiResponse.error("Une erreur interne est survenue", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
