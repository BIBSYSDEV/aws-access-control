package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Tag;
import java.nio.file.Path;
import java.util.Collections;
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

        String tableArn = environment.readEnv("TABLE_ARN");
        String roleArn = environment.readEnvOpt("ASSUMED_ROLE_ARN").orElse("NO_ASSUMED_ROLE");
        logger.info("Assuming role:"+roleArn);
        String policy = IoUtils.stringFromResources(Path.of("DynamoDbAccessPolicy.json"));
        String username = extractValidUserNameOrThrowException(requestInfo);
        logger.info("Searching for user with username:"+username);
        final String mySession = "mySession";


        STSAssumeRoleSessionCredentialsProvider credentials=
            new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, mySession)
                .withExternalId("USER#orestis")
               .withSessionTags(Collections.singletonList(new Tag().withKey("username").withValue("USER#orestis")))
            .withStsClient(stsClient).build();


        UserDto queryObject = UserDto.newBuilder().withUsername(username).build();
        databaseService.updateClient(credentials);
        return databaseService.getUser(queryObject);
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
