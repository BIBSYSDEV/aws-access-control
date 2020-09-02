package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.AssumedRoleUser;
import com.amazonaws.services.securitytoken.model.Tag;
import java.nio.file.Path;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandler extends HandlerAccessingUser<Void, UserDto> {

    public static final int MIN_DURATION_SECONDS = 900;
    private final DatabaseService databaseService;

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
        String tableArn = environment.readEnv("TABLE_ARN");
        String policy= IoUtils.stringFromResources(Path.of("DynamoDbAccessPolicy.json"));
        String username = extractValidUserNameOrThrowException(requestInfo);
        AssumeRoleRequest request = new AssumeRoleRequest()
            .withDurationSeconds(MIN_DURATION_SECONDS)
            .withTags(new Tag().withKey("username").withValue(username))
            .withTags(new Tag().withKey("tableArn").withValue(tableArn))
            .withRoleSessionName("mySession")
        .withPolicy(policy);

        AssumeRoleResult result =
            AWSSecurityTokenServiceClientBuilder.defaultClient()
                .assumeRole(request);


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
}
