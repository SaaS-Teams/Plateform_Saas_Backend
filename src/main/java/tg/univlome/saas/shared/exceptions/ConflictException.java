package tg.univlome.saas.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception lancée lors d'un conflit d'état, par exemple un doublon (HTTP 409).
 */
public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
