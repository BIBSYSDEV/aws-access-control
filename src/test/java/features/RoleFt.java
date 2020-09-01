package features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.handlers.AddRoleHandler;
import no.unit.nva.handlers.GetRoleHandler;
import no.unit.nva.model.RoleDto;
import nva.commons.handlers.GatewayResponse;
import org.apache.http.HttpStatus;

public class RoleFt extends ScenarioTest {

    /**
     * Default Constructor.
     *
     * @param scenarioContext the injected scenario context.
     */
    public RoleFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("^an (\\w*) with role-name \"(\\w*)\" that exists in the Database$")
    public void an_ExistingRole_with_role_name(String roleAlias, String roleName)
        throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        RoleDto roleDto = addRoleToExampleRoles(roleAlias, roleName);
        getDatabaseService().addRole(roleDto);
    }

    @Given("^a (\\w*) with role-name \"(.*)\" that does not exist in the Database$")
    public void a_Role_with_role_name_that_does_not_exist_in_the_Database(String roleAlias, String roleName)
        throws InvalidEntryInternalException {
        addRoleToExampleRoles(roleAlias, roleName);
    }

    @Given("^a (\\w*) with role-name \"(.*)\" that does not yet exist in the Database$")
    public void an_NewRole_with_role_name_that_does_not_yet_exist_in_the_Database(String roleAlias, String roleName)
        throws InvalidEntryInternalException {
        addRoleToExampleRoles(roleAlias, roleName);
    }

    @When("^the AuthorizedClient requests to read the (\\w*)$")
    public void the_AuthorizedClient_requests_to_read_the_Role(String roleAlias) throws IOException {
        initializeContextRequestBuilder(null);
        RoleDto role = scenarioContext.getExampleRole(roleAlias);
        scenarioContext.getRequestBuilder().withPathParameters(pathParameter(role));

        GetRoleHandler getRoleHandler = new GetRoleHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(getRoleHandler);
    }

    @When("^the AuthorizedClient requests to add the (\\w*) to the Database$")
    public void the_AuthorizedClient_requests_to_add_the_NewRole_to_the_Database(String roleAlias)
        throws IOException {
        RoleDto newRole = scenarioContext.getExampleRole(roleAlias);
        initializeContextRequestBuilder(newRole);
        AddRoleHandler addRoleHandler = new AddRoleHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(addRoleHandler);
    }

    @Then("an OK message is returned")
    public void an_OK_message_is_returned() throws IOException {
        GatewayResponse<RoleDto> response = scenarioContext.getApiGatewayResponse(RoleDto.class);
        int statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpStatus.SC_OK)));
    }

    @Then("a RoleDescription is returned")
    public void a_role_description_is_returned() throws IOException {
        RoleDto actualRoleDto = getResponseBody();
        assertThat(actualRoleDto, is(notNullValue()));
    }

    private RoleDto addRoleToExampleRoles(String roleAlias, String roleName) throws InvalidEntryInternalException {
        RoleDto roleDto = RoleDto.newBuilder().withName(roleName).build();
        scenarioContext.addExampleRole(roleAlias, roleDto);
        return roleDto;
    }

    private RoleDto getResponseBody() throws IOException {
        return getResponseBody(RoleDto.class);
    }

    private Map<String, String> pathParameter(RoleDto queryObject) {
        return Collections.singletonMap(GetRoleHandler.ROLE_PATH_PARAMETER, queryObject.getRoleName());
    }
}
