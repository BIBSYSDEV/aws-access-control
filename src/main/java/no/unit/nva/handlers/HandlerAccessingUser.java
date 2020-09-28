package no.unit.nva.handlers;

import java.nio.charset.StandardCharsets;

public interface HandlerAccessingUser {

    String USERS_RELATIVE_PATH = "/users/";
    String USERNAME_PATH_PARAMETER = "username";

    String EMPTY_USERNAME_PATH_PARAMETER_ERROR = "Path parameter \"" + USERNAME_PATH_PARAMETER + "\" cannot be empty";

    default String decodeUrlPart(String encodedString) {
        return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8);
    }
}
