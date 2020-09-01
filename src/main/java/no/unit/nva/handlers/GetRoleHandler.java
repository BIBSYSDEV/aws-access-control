package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.model.RoleDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRoleHandler extends ApiGatewayHandler<Void, RoleDto> {

    public static final String ROLE_NOT_FOUND_ERROR_MESSAGE = "Could not find role: ";
    public static final String EMPTY_ROLE_NAME = "Role-name cannot be empty";
    public static final String ROLE_PATH_PARAMETER = "role";
    private static final Logger logger = LoggerFactory.getLogger(GetRoleHandler.class);

    private final DatabaseService databaseService;

    /**
     * Default constructor used by AWS Lambda.
     */
    @JacocoGenerated
    public GetRoleHandler() {
        this(new Environment(), new DatabaseServiceImpl());
    }

    public GetRoleHandler(Environment environment, DatabaseService databaseService) {
        super(Void.class, environment, logger);
        this.databaseService = databaseService;
    }

    @Override
    public RoleDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        String roleName = roleNameThatIsNotNullOrBlank(requestInfo);

        RoleDto searchObject = RoleDto.newBuilder().withName(roleName).build();
        return databaseService.getRole(searchObject);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, RoleDto output) {
        return HttpStatus.SC_OK;
    }

    private String roleNameThatIsNotNullOrBlank(RequestInfo requestInfo) throws BadRequestException {
        return Optional.ofNullable(requestInfo.getPathParameters())
            .map(pathParams -> pathParams.get(ROLE_PATH_PARAMETER))
            .filter(not(String::isBlank))
            .orElseThrow(() -> new BadRequestException(EMPTY_ROLE_NAME));
    }
}
