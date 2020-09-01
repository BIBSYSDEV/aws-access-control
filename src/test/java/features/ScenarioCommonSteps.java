package features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.IOException;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.model.UserDto;
import nva.commons.handlers.GatewayResponse;
import org.apache.http.HttpStatus;
import org.zalando.problem.Problem;

public class ScenarioCommonSteps extends DatabaseAccessor {

    private final ScenarioContext scenarioContext;

    public ScenarioCommonSteps(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("a Database for users and roles")
    public void a_database_for_users_and_roles() {
        this.scenarioContext.setDatabaseService(createDatabaseServiceUsingLocalStorage());
    }

    @Given("an AuthorizedClient that is authorized through Feide")
    public void an_AuthorizedClient_that_is_authorized_through_Feide() {
        //DO NOTHING
    }

    @Then("a NotFound message is returned")
    public void a_NotFound_message_is_returned() throws IOException {
        GatewayResponse<Problem> response = scenarioContext.getApiGatewayResponse(Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_NOT_FOUND)));
    }

    @Then("a BadRequest message is returned containing information about the invalid request")
    public void a_BadRequest_message_is_returned() throws IOException {
        GatewayResponse<Problem> response = scenarioContext.getApiGatewayResponse(Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
        Problem problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), containsString(UserDto.INVALID_USER_ERROR_MESSAGE));
    }

    /**
     * After each scenario close the local database.
     */
    @After
    @Override
    public void closeDB() {
        super.closeDB();
    }
}
