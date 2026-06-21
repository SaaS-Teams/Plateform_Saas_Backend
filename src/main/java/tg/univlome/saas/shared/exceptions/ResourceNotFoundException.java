package tg.univlome.saas.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception lancée lorsqu'une ressource n'est pas trouvée (HTTP 404).
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
