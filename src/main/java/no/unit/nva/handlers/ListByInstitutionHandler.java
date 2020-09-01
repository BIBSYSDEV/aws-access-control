package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.List;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.model.UserDto;
import no.unit.nva.model.UserList;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class ListByInstitutionHandler extends ApiGatewayHandler<Void, UserList> {

    public static final String INSTITUTION_ID_PATH_PARAMETER = "institution";
    public static final String MISSING_PATH_PARAMETER_ERROR = "Missing institution path parameter. "
        + "Probably error in the Lambda function definition.";
    private final DatabaseService databaseService;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public ListByInstitutionHandler() {
        this(new Environment(), new DatabaseServiceImpl());
    }

    public ListByInstitutionHandler(Environment environment, DatabaseService databaseService) {
        super(Void.class, environment, LoggerFactory.getLogger(ListByInstitutionHandler.class));
        this.databaseService = databaseService;
    }

    @Override
    protected UserList processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        String institutionId = extractInstitutionIdFromRequest(requestInfo);
        List<UserDto> users = databaseService.listUsers(institutionId);
        return UserList.fromList(users);
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
