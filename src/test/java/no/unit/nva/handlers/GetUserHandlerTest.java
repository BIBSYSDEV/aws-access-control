package no.unit.nva.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.TypedObjectsDetails;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.GatewayResponse;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GetUserHandlerTest extends HandlerTest {

    private static final String BLANK_STRING = " ";

    private RequestInfo requestInfo;
    private Context context;
    private GetUserHandler getUserHandler;

    @BeforeEach
    public void init() {
        databaseService = createDatabaseServiceUsingLocalStorage();
        AWSSecurityTokenService stsService = mockStsService();
        getUserHandler = new GetUserHandler(envWithTableName, databaseService,stsService);
        context = mock(Context.class);
    }

    private AWSSecurityTokenService mockStsService() {
        AWSSecurityTokenService sts = mock(AWSSecurityTokenService.class);
        when(sts.assumeRole(any(AssumeRoleRequest.class)))
            .thenReturn(mockAssumeRole());
        return sts;
    }

    private AssumeRoleResult mockAssumeRole() {
        return new AssumeRoleResult().withCredentials(
            mockCredentials());
    }




    @DisplayName("handleRequest returns User object with type \"User\"")
    @Test
    public void handleRequestReturnsUserObjectWithTypeRole()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException, IOException {
        insertSampleUserToDatabase();

        ByteArrayOutputStream outputStream = sendGetUserRequestToHandler();

        GatewayResponse<ObjectNode> response = GatewayResponse.fromOutputStream(outputStream);
        ObjectNode bodyObject = response.getBodyObject(ObjectNode.class);

        assertThat(bodyObject.get(TypedObjectsDetails.TYPE_ATTRIBUTE), is(not(nullValue())));
        String type = bodyObject.get(TypedObjectsDetails.TYPE_ATTRIBUTE).asText();
        assertThat(type, is(equalTo(UserDto.TYPE)));
    }

    private ByteArrayOutputStream sendGetUserRequestToHandler() throws IOException {
        requestInfo = createRequestInfoForGetUser(DEFAULT_USERNAME);
        InputStream inputStream = new HandlerRequestBuilder<Void>(JsonUtils.objectMapper)
            .withPathParameters(requestInfo.getPathParameters())
            .build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getUserHandler.handleRequest(inputStream, outputStream, context);
        return outputStream;
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        Integer actual = getUserHandler.getSuccessStatusCode(null, null);
        assertThat(actual, is(equalTo(HttpStatus.SC_OK)));
    }

    @DisplayName("processInput() returns UserDto when path parameter contains the username of an existing user")
    @Test
    void processInputReturnsUserDtoWhenPathParameterContainsTheUsernameOfExistingUser() throws ApiGatewayException {
        requestInfo = createRequestInfoForGetUser(DEFAULT_USERNAME);
        UserDto expected = insertSampleUserToDatabase();
        UserDto actual = getUserHandler.processInput(null, requestInfo, context);
        assertThat(actual, is(equalTo(expected)));
    }

    @DisplayName("processInput() handles encoded path parameters")
    @Test
    void processInputReturnsUserDtoWhenPathParameterContainsTheUsernameOfExistingUserEnc() throws ApiGatewayException {

        String encodedUserName = encodeString(DEFAULT_USERNAME);
        requestInfo = createRequestInfoForGetUser(encodedUserName);
        UserDto expected = insertSampleUserToDatabase();
        UserDto actual = getUserHandler.processInput(null, requestInfo, context);
        assertThat(actual, is(equalTo(expected)));
    }

    @DisplayName("processInput() throws NotFoundException when path parameter is a string that is not an existing "
        + "username")
    @Test
    void processInputThrowsNotFoundExceptionWhenPathParameterIsNonExistingUsername() {
        requestInfo = createRequestInfoForGetUser(DEFAULT_USERNAME);
        Executable action = () -> getUserHandler.processInput(null, requestInfo, context);
        assertThrows(NotFoundException.class, action);
    }

    @DisplayName("processInput() throws BadRequestException when path parameter is a blank string")
    @Test
    void processInputThrowBadRequestExceptionWhenPathParameterIsBlank() {
        requestInfo = createRequestInfoForGetUser(BLANK_STRING);
        Executable action = () -> getUserHandler.processInput(null, requestInfo, context);
        assertThrows(BadRequestException.class, action);
    }

    @DisplayName("processInput() throws BadRequestException when path parameter is null")
    @Test
    void processInputThrowBadRequestExceptionWhenPathParameterIsNull() {
        requestInfo = createRequestInfoForGetUser(null);
        Executable action = () -> getUserHandler.processInput(null, requestInfo, context);
        assertThrows(BadRequestException.class, action);
    }

    private RequestInfo createRequestInfoForGetUser(String username) {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setPathParameters(Collections.singletonMap(GetUserHandler.USERNAME_PATH_PARAMETER, username));
        return reqInfo;
    }
}