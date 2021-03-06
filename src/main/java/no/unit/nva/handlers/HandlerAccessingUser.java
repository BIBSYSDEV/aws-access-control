package no.unit.nva.handlers;

import java.nio.charset.StandardCharsets;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.utils.Environment;
import org.slf4j.Logger;

public abstract class HandlerAccessingUser<I, O> extends ApiGatewayHandler<I, O> {

    public static String USERS_RELATIVE_PATH = "/users/";
    public static String USERNAME_PATH_PARAMETER = "username";

    public static final String EMPTY_USERNAME_PATH_PARAMETER_ERROR =
        "Path parameter \"" + USERNAME_PATH_PARAMETER + "\" cannot be empty";

    public HandlerAccessingUser(Class<I> iclass, Environment environment, Logger logger) {
        super(iclass, environment, logger);
    }

    protected String decodeUrlPart(String encodedString) {
        return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8);
    }
}
