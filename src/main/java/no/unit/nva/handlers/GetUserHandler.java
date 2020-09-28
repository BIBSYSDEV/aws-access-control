package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandler extends AuthorizedHandler<Void, UserDto> implements HandlerAccessingUser {

    public static final int MIN_DURATION_SECONDS = 900;
    private final DatabaseService databaseService;

    @JacocoGenerated
    public GetUserHandler() {
        this(new Environment(),
            new DatabaseServiceImpl(),
            AWSSecurityTokenServiceClientBuilder.defaultClient()
        );
    }

    public GetUserHandler(Environment environment, DatabaseService databaseService, AWSSecurityTokenService stsClient) {
        super(Void.class, environment, stsClient, defaultLogger());
        this.databaseService = databaseService;
    }

    @Override
    protected UserDto processInput(Void input,
                                   RequestInfo requestInfo,
                                   STSAssumeRoleSessionCredentialsProvider credentialsProvider,
                                   Context context)
        throws ApiGatewayException {
        databaseService.login(credentialsProvider);
        var requestedUsername = extractValidUsernameFromPathParameters(requestInfo);
        var queryObject = UserDto.newBuilder().withUsername(requestedUsername).build();

        return databaseService.getUser(queryObject);
    }

    @Override
    protected List<Tag> sessionTags(RequestInfo requestInfo) {
        Tag usernameTag = new Tag().withKey("username").withValue(requestInfo.getUsername().orElseThrow());
        return Collections.singletonList(usernameTag);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserDto output) {
        return HttpStatus.SC_OK;
    }

    private String extractValidUsernameFromPathParameters(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(requestInfo)
            .map(RequestInfo::getPathParameters)
            .map(map -> map.get(USERNAME_PATH_PARAMETER))
            .map(this::decodeUrlPart)
            .filter(not(String::isBlank))
            .orElseThrow(() -> new BadRequestException(EMPTY_USERNAME_PATH_PARAMETER_ERROR));
    }

    private static Logger defaultLogger() {
        return LoggerFactory.getLogger(GetUserHandler.class);
    }
}
