package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.services.lambda.runtime.Context;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandler extends ApiGatewayHandler<Void, UserDto> {

    private final DatabaseService databaseService;

    public static String USERS_RELATIVE_PATH = "/users/";
    public static String USERNAME_PATH_PARAMETER = "username";

    public static final String EMPTY_USERNAME_PATH_PARAMETER_ERROR =
        "Path parameter \"" + USERNAME_PATH_PARAMETER + "\" cannot be empty";



    @JacocoGenerated
    public GetUserHandler() {
        this(new Environment(), new DatabaseServiceImpl());
    }

    public GetUserHandler(Environment environment, DatabaseService databaseService) {
        super(Void.class, environment, defaultLogger());
        this.databaseService = databaseService;
    }

    @Override
    protected UserDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        String username = extractValidUserNameOrThrowException(requestInfo);
        UserDto queryObject = UserDto.newBuilder().withUsername(username).build();
        return databaseService.getUser(queryObject);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserDto output) {
        return HttpStatus.SC_OK;
    }

    private static Logger defaultLogger() {
        return LoggerFactory.getLogger(GetUserHandler.class);
    }

    private String extractValidUserNameOrThrowException(RequestInfo requestInfo) throws BadRequestException {

        return Optional.of(requestInfo)
            .map(RequestInfo::getPathParameters)
            .map(map -> map.get(USERNAME_PATH_PARAMETER))
            .map(this::decodeUrlPart)
            .filter(not(String::isBlank))
            .orElseThrow(() -> new BadRequestException(EMPTY_USERNAME_PATH_PARAMETER_ERROR));
    }

    protected String decodeUrlPart(String encodedString) {
        return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8);
    }
}
