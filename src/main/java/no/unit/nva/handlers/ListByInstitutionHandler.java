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
import no.unit.nva.model.UserDto;
import no.unit.nva.model.UserList;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListByInstitutionHandler extends AuthorizedHandler<Void, UserList> {

    public static final String INSTITUTION_ID_PATH_PARAMETER = "institution";
    public static final String MISSING_PATH_PARAMETER_ERROR = "Missing institution path parameter. "
        + "Probably error in the Lambda function definition.";
    private final DatabaseService databaseService;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public ListByInstitutionHandler() {
        this(
            new Environment(),
            new DatabaseServiceImpl(),
            AWSSecurityTokenServiceClientBuilder.defaultClient()
        );
    }

    public ListByInstitutionHandler(Environment environment,
                                    DatabaseService databaseService,
                                    AWSSecurityTokenService sts) {
        super(Void.class, environment, sts, defaultLogger());
        this.databaseService = databaseService;
    }

    private static Logger defaultLogger() {
        return LoggerFactory.getLogger(ListByInstitutionHandler.class);
    }

    @Override
    protected UserList processInput(Void input,
                                    RequestInfo requestInfo,
                                    STSAssumeRoleSessionCredentialsProvider credentials,
                                    Context context)
        throws ApiGatewayException {
        String institutionId = extractInstitutionIdFromRequest(requestInfo);
        databaseService.login(credentials);
        List<UserDto> users = databaseService.listUsers(institutionId);
        return UserList.fromList(users);
    }

    @Override
    protected List<Tag> sessionTags(RequestInfo requestInfo) {
        Tag usernameTag = new Tag().withKey("username").withValue(requestInfo.getUsername().orElseThrow());
        return Collections.singletonList(usernameTag);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserList output) {
        return HttpStatus.SC_OK;
    }

    private String extractInstitutionIdFromRequest(RequestInfo requestInfo) {
        return Optional.of(requestInfo)
            .map(RequestInfo::getPathParameters)
            .map(pathParams -> pathParams.get(INSTITUTION_ID_PATH_PARAMETER))
            .filter(not(String::isBlank))
            .orElseThrow(() -> new IllegalStateException(MISSING_PATH_PARAMETER_ERROR));
    }
}
