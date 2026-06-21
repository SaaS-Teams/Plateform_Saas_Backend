package tg.univlome.saas.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception de base pour toutes les exceptions métiers.
 */
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
