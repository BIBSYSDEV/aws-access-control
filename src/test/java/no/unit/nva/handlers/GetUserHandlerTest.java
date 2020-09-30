package no.unit.nva.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.mocks.MockStsClient;
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
import org.zalando.problem.Problem;

class GetUserHandlerTest extends HandlerTest {

    private static final String BLANK_STRING = " ";
    public static final Void INPUT = null;
    public static final STSAssumeRoleSessionCredentialsProvider EMPTY_CREDENTIALS = null;

    private RequestInfo requestInfo;
    private Context context;
    private GetUserHandler getUserHandler;

    @BeforeEach
    public void init() {
        databaseService = createDatabaseServiceUsingLocalStorage();
        AWSSecurityTokenService stsService = new MockStsClient();
        getUserHandler = new GetUserHandler(envWithTableName, databaseService,stsService);
        context = mock(Context.class);
    }



    @Test
    public void handleRequestReturnsOkForValidRequest()
        throws IOException, ConflictException, InvalidEntryInternalException, InvalidInputException {
        UserDto expected = insertSampleUserToDatabase();

        ByteArrayOutputStream output = sendGetUserRequestToHandler();
        GatewayResponse<UserDto> response = GatewayResponse.fromOutputStream(output);
        UserDto actualUser = response.getBodyObject(UserDto.class);
        assertThat(response.getStatusCode(),is(equalTo(HttpStatus.SC_OK)));
        assertThat(actualUser,is(equalTo(expected)));

    }

    private ByteArrayOutputStream sendGetUserRequestToHandler()
        throws IOException {
        requestInfo = createRequestInfoForGetUser(DEFAULT_USERNAME);
        InputStream inputStream = new HandlerRequestBuilder<Void>(JsonUtils.objectMapper)
            .withPathParameters(requestInfo.getPathParameters())
            .withFeideId("feide@id")
            .withCustomerId("customerId")
            .build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getUserHandler.handleRequest(inputStream, outputStream, context);
        return outputStream;
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        Integer actual = getUserHandler.getSuccessStatusCode(INPUT, null);
        assertThat(actual, is(equalTo(HttpStatus.SC_OK)));
    }

    @DisplayName("processInput() returns UserDto when path parameter contains the username of an existing user")
    @Test
    void processInputReturnsUserDtoWhenPathParameterContainsTheUsernameOfExistingUser() throws ApiGatewayException {
        requestInfo = createRequestInfoForGetUser(DEFAULT_USERNAME);
        UserDto expected = insertSampleUserToDatabase();
        UserDto actual = getUserHandler.processInput(INPUT, requestInfo, null,context);
        assertThat(actual, is(equalTo(expected)));
    }

    @DisplayName("processInput() handles encoded path parameters")
    @Test
    void processInputReturnsUserDtoWhenPathParameterContainsTheUsernameOfExistingUserEnc() throws ApiGatewayException {

        String encodedUserName = encodeString(DEFAULT_USERNAME);
        requestInfo = createRequestInfoForGetUser(encodedUserName);
        UserDto expected = insertSampleUserToDatabase();
        UserDto actual = getUserHandler.processInput(INPUT, requestInfo, null,context);
        assertThat(actual, is(equalTo(expected)));
    }

    @DisplayName("processInput() throws NotFoundException when path parameter is a string that is not an existing "
        + "username")
    @Test
    void processInputThrowsNotFoundExceptionWhenPathParameterIsNonExistingUsername() {
        requestInfo = createRequestInfoForGetUser(DEFAULT_USERNAME);
        Executable action = () -> getUserHandler.processInput(INPUT, requestInfo,null, context);
        assertThrows(NotFoundException.class, action);
    }


    @DisplayName("processInput() throws BadRequestException when path parameter is null")
    @Test
    void processInputThrowBadRequestExceptionWhenPathParameterIsNull() {
        requestInfo = createRequestInfoForGetUser(null);
        Executable action = () -> getUserHandler.processInput(INPUT, requestInfo, EMPTY_CREDENTIALS, context);
        assertThrows(BadRequestException.class, action);
    }

    private RequestInfo createRequestInfoForGetUser(String username) {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setPathParameters(Collections.singletonMap(GetUserHandler.USERNAME_PATH_PARAMETER, username));
        RequestInfo mockReqInfo = spy(reqInfo);

        Optional<String> loggedInUser = Optional.ofNullable(username);
        if(loggedInUser.isEmpty()){
            loggedInUser= Optional.of("someUser");
        }
        when(mockReqInfo.getUsername()).thenReturn(loggedInUser);
        return mockReqInfo;
    }
}