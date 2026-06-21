package tg.univlome.saas.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception lancée lorsqu'une règle métier n'est pas respectée (HTTP 422).
 */
public class BusinessRuleException extends BaseException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
