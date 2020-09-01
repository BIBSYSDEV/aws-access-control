package features;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JavaType;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.JsonUtils;

public class ScenarioContext {

    private HandlerRequestBuilder<Map<String, Object>> requestBuilder;
    private String requestResponse;
    private DatabaseServiceImpl databaseService;
    private final Map<String, UserDto> exampleUsers;
    private final Map<String, RoleDto> exampleRoles;

    public ScenarioContext() {
        this.exampleUsers = new ConcurrentHashMap<>();
        this.exampleRoles = new ConcurrentHashMap<>();
    }

    public void addExampleUser(String userAlias, UserDto existingUser) {
        exampleUsers.put(userAlias, existingUser);
    }

    public UserDto getExampleUser(String userAlias) {
        return exampleUsers.get(userAlias);
    }

    public void replaceUser(String userAlias, UserDto user) {
        exampleUsers.put(userAlias, user);
    }

    public void addExampleRole(String alias, RoleDto roleDto) {
        exampleRoles.put(alias, roleDto);
    }

    public RoleDto getExampleRole(String roleAlias) {
        return exampleRoles.get(roleAlias);
    }

    protected HandlerRequestBuilder<Map<String, Object>> getRequestBuilder() {
        if (isNull(requestBuilder)) {
            requestBuilder = new HandlerRequestBuilder<>(JsonUtils.objectMapper);
        }
        return requestBuilder;
    }

    protected void setRequestBuilder(HandlerRequestBuilder<Map<String, Object>> requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    protected DatabaseServiceImpl getDatabaseService() {
        return databaseService;
    }

    protected void setDatabaseService(DatabaseServiceImpl databaseService) {
        this.databaseService = databaseService;
    }

    protected <T> GatewayResponse<T> getApiGatewayResponse(Class<T> responseBodyClass) throws IOException {
        JavaType typeRef = JsonUtils.objectMapper.getTypeFactory()
            .constructParametricType(GatewayResponse.class, responseBodyClass);
        return JsonUtils.objectMapper.readValue(requestResponse, typeRef);
    }

    protected <T> T getResponseBody(Class<T> requestBodyClass) throws IOException {
        return getApiGatewayResponse(requestBodyClass).getBodyObject(requestBodyClass);
    }

    protected void setRequestResponse(String requestResponse) {
        this.requestResponse = requestResponse;
    }
}
