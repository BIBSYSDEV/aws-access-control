package no.unit.nva.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.interfaces.WithEnvironment;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.TypedObjectsDetails;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.GatewayResponse;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.http.HttpMethods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GetRoleHandlerTest extends DatabaseAccessor implements WithEnvironment {

    public static final String THE_ROLE = "theRole";
    public static final String BLANK_STR = " ";
    private DatabaseServiceImpl databaseService;
    private GetRoleHandler getRoleHandler;
    private Context context;

    /**
     * init.
     */
    @BeforeEach
    public void init() {
        databaseService = createDatabaseServiceUsingLocalStorage();
        getRoleHandler = new GetRoleHandler(mockEnvironment(), databaseService);
        context = mock(Context.class);
    }

    @Test
    public void statusCodeReturnsOkWhenRequestIsSuccessful() {
        Integer successCode = getRoleHandler.getSuccessStatusCode(null, null);
        assertThat(successCode, is(equalTo(HttpStatus.SC_OK)));
    }

    @DisplayName("handleRequest returns Role object with type \"Role\"")
    @Test
    public void handleRequestReturnsRoleObjectWithTypeRole()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException, IOException {

        addSampleRoleToDatabase();

        ByteArrayOutputStream outputStream = sendGetRoleRequest();
        ObjectNode bodyObject = extractBodyFromResponseAsJsonObject(outputStream);

        assertThat(bodyObject.get(TypedObjectsDetails.TYPE_ATTRIBUTE), is(not(nullValue())));

        String type = bodyObject.get(TypedObjectsDetails.TYPE_ATTRIBUTE).asText();
        assertThat(type, is(equalTo(RoleDto.TYPE)));
    }

    private ObjectNode extractBodyFromResponseAsJsonObject(ByteArrayOutputStream outputStream)
        throws com.fasterxml.jackson.core.JsonProcessingException {
        GatewayResponse<ObjectNode> response = GatewayResponse.fromOutputStream(outputStream);

        return response.getBodyObject(ObjectNode.class);
    }

    @DisplayName("processInput returns RoleDto when a role with the input role-name exists")
    @Test
    void processInputReturnsRoleDtoWhenARoleWithTheInputRoleNameExists()
        throws ApiGatewayException {
        addSampleRoleToDatabase();
        RequestInfo requestInfo = queryWithRoleName(THE_ROLE);
        RoleDto roleDto = getRoleHandler.processInput(null, requestInfo, context);
        assertThat(roleDto.getRoleName(), is(equalTo(THE_ROLE)));
    }

    @DisplayName("processInput() throws NotFoundException when there is no role with the input role-name")
    @Test
    void processInputThrowsNotFoundExceptionWhenThereIsNoRoleInTheDatabaseWithTheSpecifiedRoleName() {
        RequestInfo requestInfo = queryWithRoleName(THE_ROLE);
        Executable action = () -> getRoleHandler.processInput(null, requestInfo, context);
        NotFoundException exception = assertThrows(NotFoundException.class, action);
        assertThat(exception.getMessage(), containsString(THE_ROLE));
    }

    @Test
    @DisplayName("processInput() throws BadRequestException when no role-name is provided")
    void processInputThrowsBadRequestExceptionWhenNoRoleNameIsProvided() {
        RequestInfo requestInfoWithoutRoleName = new RequestInfo();
        Executable action = () -> getRoleHandler.processInput(null, requestInfoWithoutRoleName, context);
        BadRequestException exception = assertThrows(BadRequestException.class, action);
        assertThat(exception.getMessage(), containsString(GetRoleHandler.EMPTY_ROLE_NAME));
    }

    @Test
    @DisplayName("processInput() throws BadRequestException when blank role-name is provided")
    void processInputThrowsBadRequestExceptionWhenBlankRoleNameIsProvided() {
        RequestInfo requestInfoWithBlankRoleName = queryWithRoleName(BLANK_STR);
        Executable action = () -> getRoleHandler.processInput(null, requestInfoWithBlankRoleName, context);
        BadRequestException exception = assertThrows(BadRequestException.class, action);
        assertThat(exception.getMessage(), containsString(GetRoleHandler.EMPTY_ROLE_NAME));
    }

    private ByteArrayOutputStream sendGetRoleRequest() throws IOException {
        RequestInfo requestInfo = queryWithRoleName(THE_ROLE);
        InputStream requestStream = new HandlerRequestBuilder<>(JsonUtils.objectMapper)
            .withPathParameters(requestInfo.getPathParameters())
            .withHttpMethod(HttpMethods.GET)
            .build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getRoleHandler.handleRequest(requestStream, outputStream, context);
        return outputStream;
    }

    private RequestInfo queryWithRoleName(String roleName) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.getPathParameters().put(GetRoleHandler.ROLE_PATH_PARAMETER, roleName);
        return requestInfo;
    }

    private void addSampleRoleToDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        RoleDto existingRole = RoleDto.newBuilder().withName(GetRoleHandlerTest.THE_ROLE).build();
        databaseService.addRole(existingRole);
    }
}