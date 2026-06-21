package tg.univlome.saas.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception lancée lorsque l'utilisateur est authentifié mais n'a pas les droits requis (HTTP 403).
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
