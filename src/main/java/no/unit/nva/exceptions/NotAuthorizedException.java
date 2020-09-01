package no.unit.nva.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class NotAuthorizedException extends ApiGatewayException {

    public NotAuthorizedException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_UNAUTHORIZED;
    }
}
