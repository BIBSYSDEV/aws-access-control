package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Tag;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.NotAuthorizedException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandler extends HandlerAccessingUser<Void, UserDto> {

    public static final int MIN_DURATION_SECONDS = 900;
    private final DatabaseService databaseService;
    private final AWSSecurityTokenService stsClient;

    @JacocoGenerated
    public GetUserHandler() {
        this(new Environment(),
            new DatabaseServiceImpl(),
            AWSSecurityTokenServiceClientBuilder.defaultClient()
        );
    }

    public GetUserHandler(Environment environment, DatabaseService databaseService, AWSSecurityTokenService stsClient) {
        super(Void.class, environment, defaultLogger());
        this.databaseService = databaseService;
        this.stsClient = stsClient;
    }

    private static Logger defaultLogger() {
        return LoggerFactory.getLogger(GetUserHandler.class);
    }

    @Override
    protected UserDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        String roleArn = environment.readEnvOpt("ASSUMED_ROLE_ARN").orElse("NO_ASSUMED_ROLE");



        String requestedUser = extractValidUserNameOrThrowException(requestInfo);
        String loggedInUser = requestInfo.getUsername().orElseThrow(this::handleMissingUsername);
        logger.info("Username:"+loggedInUser);
        final String mySession = "mySession";

        STSAssumeRoleSessionCredentialsProvider credentials=
            new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, mySession)
               .withSessionTags(Collections.singletonList(new Tag().withKey("username").withValue(loggedInUser)))
            .withStsClient(stsClient).build();


        UserDto queryObject = UserDto.newBuilder().withUsername(requestedUser).build();
        databaseService.login(credentials);
        List<UserDto> users = databaseService.listUsers(
            "https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934");
        try {
            String jsonUsers= JsonUtils.objectMapper.writeValueAsString(users);
            logger.info(jsonUsers);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return databaseService.getUser(queryObject);
    }

    private NotAuthorizedException handleMissingUsername() {
        return new NotAuthorizedException("Anonymous user not authorized");
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserDto output) {
        return HttpStatus.SC_OK;
    }

    private String extractValidUserNameOrThrowException(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(requestInfo)
            .map(RequestInfo::getPathParameters)
            .map(map -> map.get(USERNAME_PATH_PARAMETER))
            .map(this::decodeUrlPart)
            .filter(not(String::isBlank))
            .orElseThrow(() -> new BadRequestException(EMPTY_USERNAME_PATH_PARAMETER_ERROR));
    }
}
