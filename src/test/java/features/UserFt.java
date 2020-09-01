package features;

import static java.util.Objects.isNull;
import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.handlers.AddUserHandler;
import no.unit.nva.handlers.GetUserHandler;
import no.unit.nva.handlers.HandlerAccessingUser;
import no.unit.nva.handlers.ListByInstitutionHandler;
import no.unit.nva.handlers.UpdateUserHandler;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.model.UserList;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.EntityUtils;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.attempt.Try;

public class UserFt extends ScenarioTest {

    public static final int SIZE_IS_NOT_IMPORTANT = 0;

    private UserList existingUsers;

    public UserFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("^a(?:n){0,1} (\\w*) with username \"(.*)\" that exists in the Database$")
    public void a_user_with_username_that_exists_in_the_Database(String userAlias, String username)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto existingUser = UserDto.newBuilder().withUsername(username).build();
        scenarioContext.getDatabaseService().addUser(existingUser);
        scenarioContext.addExampleUser(userAlias, existingUser);
    }

    @Given("^the (.*) has the roles:$")
    public void the_user_has_the_roles(String userAlias, DataTable dataTable)
        throws InvalidInputException, InvalidEntryInternalException, NotFoundException {
        List<RoleDto> roles = createRolesFromRoleNames(ignoreHeadersRow(dataTable));
        UserDto userUpdate = scenarioContext.getExampleUser(userAlias).copy().withRoles(roles).build();
        this.scenarioContext.getDatabaseService().updateUser(userUpdate);
        this.scenarioContext.replaceUser(userAlias, userUpdate);
    }

    @Given("^a (.*) with username \"(.*)\" that does not exist in the database$")
    public void a_NonExistingUser_with_username_that_does_not_exist_in_the_database(String userAlias, String username)
        throws InvalidEntryInternalException {
        UserDto nonExistingUser = UserDto.newBuilder().withUsername(username).build();
        scenarioContext.addExampleUser(userAlias, nonExistingUser);
    }

    @Given("^a non-existing (\\w*) with username (.*) that does not yet exist in the database$")
    public void a_non_existing_NewUser_with_username_that_does_not_yet_exist_in_the_database(String userAlias,
                                                                                             String username)
        throws InvalidEntryInternalException {
        UserDto nonExistingUser = UserDto.newBuilder().withUsername(username).withInstitution("inst").build();
        scenarioContext.addExampleUser(userAlias, nonExistingUser);
    }

    @Given("^(.*) is specified that it will have the roles:")
    public void newUser_is_specified_that_it_will_have_the_roles(String userAlias, DataTable dataTable)
        throws InvalidEntryInternalException {
        List<RoleDto> roles = createRolesFromRoleNames(ignoreHeadersRow(dataTable));
        UserDto updatedNewUser = scenarioContext.getExampleUser(userAlias).copy().withRoles(roles).build();
        scenarioContext.replaceUser(userAlias, updatedNewUser);
    }

    @Given("^the (.*) belongs to the institution \"(\\w*)\"$")
    public void the_ExistingUser_belongs_to_the_institution(String userAlias, String insitutionId)
        throws InvalidInputException, InvalidEntryInternalException, NotFoundException {
        UserDto existingUser = scenarioContext.getExampleUser(userAlias);
        UserDto userUpdate = existingUser.copy().withInstitution(insitutionId).build();
        updateUserInDatababase(userUpdate);
        scenarioContext.replaceUser(userAlias, userUpdate);
    }

    @Then("^the (\\w*) contains only the following roles:$")
    public void the_user_entry_contains_a_list_of_roles_with_the_following_role_names(String userAlias,
                                                                                      DataTable dataTable)
        throws InvalidEntryInternalException, NotFoundException {

        List<RoleDto> expectedRoles = createRolesFromDatatable(dataTable);
        UserDto queryObject = scenarioContext.getExampleUser(userAlias);
        UserDto updatedUser = getUserDirectlyFromDatabase(queryObject);

        assertThatExpectedRolesAndActualRolesAreEqual(expectedRoles, updatedUser);
    }

    @When("^the AuthorizedClient sends a request to add the (\\w*) to the Database$")
    public void the_AuthorizedClient_sends_a_request_to_add_the_NewUser_to_the_Database(String userAlias)
        throws IOException {
        initializeContextRequestBuilder(scenarioContext.getExampleUser(userAlias));
        AddUserHandler addUserHandler = new AddUserHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(addUserHandler);
    }

    @When("^the AuthorizedClient sends a request to read the (\\w*) from the Database$")
    public void the_AuthorizedClient_sends_a_request_to_read_the_user_from_the_Database(String userAlias)
        throws IOException {

        UserDto queryObject = scenarioContext.getExampleUser(userAlias);
        initializeContextRequestBuilder(null);
        addPathParameterToRequest(queryObject);
        GetUserHandler getUserHandler = new GetUserHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(getUserHandler);
    }

    @When("^the AuthorizedClient requests to update the (\\w*) setting the following roles:$")
    public void the_AuthorizedClient_requests_to_update_the_ExistingUser_and_set_the_following_roles(
        String userAlias, DataTable roleNames)
        throws InvalidEntryInternalException, NotFoundException, IOException {

        UserDto userUpdate = userEntryUpdatedWithNewRoles(userAlias, roleNames);
        initializeContextRequestBuilder(userUpdate);
        addPathParameterToRequest(userUpdate);
        sendUserUpdateRequest();
    }

    @When("^the AuthorizedClient requests to update the (\\w*)$")
    public void the_authorized_client_requests_to_update_the_nonExistingUser(String userAlias)
        throws IOException {

        UserDto requestObject = scenarioContext.getExampleUser(userAlias);
        initializeContextRequestBuilder(requestObject);
        addPathParameterToRequest(requestObject);
        sendUserUpdateRequest();
    }

    @When("^the AuthorizedClient requests to update the (.*) using a malformed body$")
    public void the_AuthorizedClient_requests_to_update_the_ExistingUser_using_a_malformed_body(String userAlias)
        throws InvalidEntryInternalException, InvocationTargetException, NoSuchMethodException, IllegalAccessException,
               IOException {

        requestBodyContainsInvalidUser();
        pathParameterPointsToExistingUser(userAlias);

        UpdateUserHandler handler = newUpdateUserHandler();
        handlerSendsRequestAndUpdatesResponse(handler);
    }

    @When("the AuthorizedClient sends the request to list the users of the {string}")
    public void the_AuthorizedClient_sends_the_request_to_list_the_users_of_the(String institution) throws IOException {

        initializeContextRequestBuilder(null);
        addInstitutionToPathParameters(institution);
        ListByInstitutionHandler handler = new ListByInstitutionHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(handler);
    }

    @Then("^the (\\w*) is updated asynchronously$")
    public void the_user_entry_is_updated_asynchronously(String userAlias)
        throws JsonProcessingException, InvalidEntryInternalException, NotFoundException {
        UserDto expectedUser = getRequestBody(getRequestBuilder(), UserDto.class);
        UserDto queryObject = scenarioContext.getExampleUser(userAlias);
        UserDto actualUser = getUserDirectlyFromDatabase(queryObject);
        assertThat(actualUser, is(equalTo(expectedUser)));
    }

    @Then("^the response has a Location header with (\\w*)'s URI as value$")
    public void a_Location_header_with_the_user_URI_is_included_in_the_response(String userAlias) throws IOException {
        GatewayResponse<UserDto> response = scenarioContext.getApiGatewayResponse(UserDto.class);
        String locationHeader = response.getHeaders().get(UpdateUserHandler.LOCATION_HEADER);
        assertThat(locationHeader, is(not(emptyOrNullString())));
        UserDto expectedUser = scenarioContext.getExampleUser(userAlias);
        assertThat(locationHeader, containsString(expectedUser.getUsername()));
    }

    @Then("a non-empty list of the users belonging to the institution is returned")
    public void a_list_of_the_users_belonging_to_the_institution_is_returned() throws IOException {
        UserList users = extractUserListFromResponse();
        assertThat(users, is(not(empty())));
    }

    @Then("the list of users should contain only the following usernames:")
    public void the_list_of_users_should_contain_only_the_following_usernames(DataTable dataTable) throws IOException {
        List<String> expectedUsernames = ignoreHeadersRow(dataTable).asList();
        UserList users = extractUserListFromResponse();
        List<String> actualUsernames = users.stream().map(UserDto::getUsername).collect(Collectors.toList());

        assertThatListsAreEquivalent(expectedUsernames, actualUsernames);
    }

    @Then("an empty list of the users belonging to the institution is returned to the client")
    public void an_empty_list_of_the_users_belonging_to_the_institution_is_returned_to_the_client() throws IOException {

        UserList users = extractUserListFromResponse();
        assertThat(users, is(empty()));
    }

    @Then("^the (\\w*) is added to the database$")
    public void the_NewUser_is_added_to_the_database(String userAlias)
        throws InvalidEntryInternalException, NotFoundException {

        UserDto newUserInDatabase = getDatabaseService()
            .getUser(scenarioContext.getExampleUser(userAlias));
        assertThat(newUserInDatabase, is(notNullValue()));
        assertThat(newUserInDatabase, is(equalTo(scenarioContext.getExampleUser(userAlias))));
    }

    @Then("^in the response, the object is the UserDescription of the (\\w*)$")
    public void the_response_object_contains_the_UserDescription(String userAlias) throws IOException {
        UserDto newUserInResponseBody = scenarioContext.getResponseBody(UserDto.class);
        assertThat(newUserInResponseBody, is(equalTo(scenarioContext.getExampleUser(userAlias))));
    }

    private void requestBodyContainsInvalidUser()
        throws InvalidEntryInternalException, NoSuchMethodException, InvocationTargetException, IllegalAccessException,
               JsonProcessingException {
        UserDto invalidUser = EntityUtils.createUserWithoutUsername();
        initializeContextRequestBuilder(invalidUser);
    }

    private void pathParameterPointsToExistingUser(String userAlias) {
        UserDto existingUser = scenarioContext.getExampleUser(userAlias);
        getRequestBuilder().withPathParameters(pathParameters(existingUser));
    }

    private List<RoleDto> createRolesFromRoleNames(DataTable dataTable) {
        return dataTable.asList().stream()
            .map(attempt(roleName -> RoleDto.newBuilder().withName(roleName).build()))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
    }

    private void addInstitutionToPathParameters(String institution) {
        getRequestBuilder().withPathParameters(
            Collections.singletonMap(
                ListByInstitutionHandler.INSTITUTION_ID_PATH_PARAMETER,
                institution));
    }

    private UpdateUserHandler newUpdateUserHandler() {
        return new UpdateUserHandler(mockEnvironment(), getDatabaseService());
    }

    private void sendUserUpdateRequest() throws IOException {
        UpdateUserHandler updateUserHandler = newUpdateUserHandler();
        handlerSendsRequestAndUpdatesResponse(updateUserHandler);
    }

    private UserDto userEntryUpdatedWithNewRoles(String userAlias, DataTable roleNames)
        throws InvalidEntryInternalException, NotFoundException {
        List<RoleDto> roles = createRolesFromDatatable(roleNames);
        UserDto existingUser = scenarioContext
            .getDatabaseService().getUser(scenarioContext.getExampleUser(userAlias));
        return existingUser.copy().withRoles(roles).build();
    }

    private HandlerRequestBuilder<Map<String, Object>> addPathParameterToRequest(UserDto queryObject) {
        return getRequestBuilder().withPathParameters(pathParameters(queryObject));
    }

    private Map<String, String> pathParameters(UserDto queryObject) {
        return Collections.singletonMap(HandlerAccessingUser.USERNAME_PATH_PARAMETER, queryObject.getUsername());
    }

    private UserList extractUserListFromResponse() throws IOException {
        GatewayResponse<UserList> response = scenarioContext.getApiGatewayResponse(UserList.class);
        return response.getBodyObject(UserList.class);
    }

    private void assertThatListsAreEquivalent(List<String> expected, List<String> actual) {
        String[] expectedArray = expected.toArray(new String[0]);
        String[] actualArray = actual.toArray(new String[0]);
        assertThat(expectedArray, is(not(emptyArray())));
        assertThat(actualArray, is(not(emptyArray())));

        // assert equivalency of lists.
        assertThat(expected, containsInAnyOrder(actualArray));
        assertThat(actual, containsInAnyOrder(expectedArray));
    }

    private void replaceUserInExistingUsersList(UserDto updatedUser) {
        existingUsers.removeIf(u -> u.getUsername().equals(updatedUser.getUsername()));
        existingUsers.add(updatedUser);
    }

    private void updateUserInDatababase(UserDto updatedUser)
        throws InvalidEntryInternalException, NotFoundException, InvalidInputException {
        getDatabaseService().updateUser(updatedUser);
    }

    private void addToExistingUsers(UserDto newUser) {
        if (isNull(existingUsers)) {
            existingUsers = new UserList();
        }
        existingUsers.add(newUser);
    }

    private Map<String, Object> createInvalidRequestBody()
        throws InvalidEntryInternalException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        UserDto invalidUser = EntityUtils.createUserWithoutUsername();
        return transformUserDtoToMap(invalidUser);
    }

    private Map<String, Object> transformUserDtoToMap(UserDto invalidUser) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        return objectMapper.convertValue(invalidUser, typeRef);
    }

    private void assertThatExpectedRolesAndActualRolesAreEqual(List<RoleDto> expectedRoles, UserDto updatedUser) {
        RoleDto[] actualRolesArray = updatedUser.getRoles().toArray(new RoleDto[SIZE_IS_NOT_IMPORTANT]);
        RoleDto[] expectedRolesArray = expectedRoles.toArray(new RoleDto[SIZE_IS_NOT_IMPORTANT]);
        assertThat(updatedUser.getRoles(), containsInAnyOrder(expectedRolesArray));
        assertThat(expectedRoles, containsInAnyOrder(actualRolesArray));
    }

    private UserDto getUserDirectlyFromDatabase(UserDto existingUser)
        throws InvalidEntryInternalException, NotFoundException {
        return getDatabaseService().getUser(existingUser);
    }

    private List<RoleDto> createRolesFromDatatable(DataTable dataTable) {
        return ignoreHeadersRow(dataTable)
            .asList()
            .stream()
            .map(attempt(roleName -> RoleDto.newBuilder().withName(roleName).build()))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
    }
}
