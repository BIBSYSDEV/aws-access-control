package no.unit.nva.model;

import static java.util.Objects.isNull;
import static nva.commons.utils.attempt.Try.attempt;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.UserDb;
import no.unit.nva.database.interfaces.WithCopy;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.StringUtils;
import nva.commons.utils.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName(UserDto.TYPE)
public class UserDto implements WithCopy<UserDto.Builder>, JsonSerializable, Validable, Typed {

    public static final String TYPE = "User";
    public static final String MISSING_FIELD_ERROR = "Invalid User. Missing obligatory field: ";
    public static final String ERROR_DUE_TO_INVALID_ROLE =
        "Failure while trying to create user with role without role-name";
    public static final String INVALID_USER_ERROR_MESSAGE = "Invalid user. User should have non-empty username.";
    private static final Logger logger = LoggerFactory.getLogger(UserDto.class);
    private List<RoleDto> roles;
    private String username;
    private String institution;

    public UserDto() {
        roles = new ArrayList<>();
    }

    private UserDto(Builder builder) {
        setUsername(builder.username);
        setInstitution(builder.institution);
        setRoles(builder.roles);
    }

    /**
     * Creates a {@link UserDto} from a {@link UserDb}.
     *
     * @param userDb a database object {@link UserDb}
     * @return a data transfer object {@link UserDto}
     * @throws InvalidEntryInternalException when database object is invalid (should never happen).
     */
    public static UserDto fromUserDb(UserDb userDb) throws InvalidEntryInternalException {

        UserDto.Builder userDto = new UserDto.Builder();
        userDto
            .withUsername(userDb.getUsername())
            .withRoles(extractRoles(userDb))
            .withInstitution(userDb.getInstitution());
        return userDto.build();
    }

    /**
     * returns a new builder.
     *
     * @return a new {@link UserDto.Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Transforms the DTO to a database object.
     *
     * @return a {@link UserDb}.
     * @throws InvalidEntryInternalException when the DTO contains an invalid user.
     */
    public UserDb toUserDb() throws InvalidEntryInternalException {
        UserDb.Builder userDb = UserDb.newBuilder()
            .withUsername(username)
            .withInstitution(institution)
            .withRoles(createRoleDb());

        return userDb.build();
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public String getInstitution() {
        return institution;
    }

    private void setInstitution(String institution) {
        this.institution = institution;
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    private void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }

    @Override
    public boolean isValid() {
        return !(isNull(username) || username.isBlank());
    }

    @Override
    public InvalidInputException exceptionWhenInvalid() {
        return new InvalidInputException(INVALID_USER_ERROR_MESSAGE);
    }

    @Override
    public String toString() {
        return toJsonString(JsonUtils.objectMapper);
    }

    /**
     * Creates a copy of the object.
     *
     * @return a Builder containing the field values of the original object.
     */
    @Override
    public UserDto.Builder copy() {
        return new Builder()
            .withUsername(username)
            .withInstitution(institution)
            .withRoles(listRoles());
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return Objects.equals(getUsername(), userDto.getUsername())
            && Objects.equals(getInstitution(), userDto.getInstitution())
            && Objects.equals(getRoles(), userDto.getRoles());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getUsername(), getInstitution(), getRoles());
    }

    private List<RoleDto> listRoles() {
        return new ArrayList<>(Optional.ofNullable(roles).orElse(Collections.emptyList()));
    }

    private static List<RoleDto> extractRoles(UserDb userDb) {
        return Optional.ofNullable(userDb)
            .stream()
            .flatMap(userDb1 -> userDb1.getRoles().stream())
            .map(attempt(RoleDto::fromRoleDb))
            .map(attempt -> attempt.orElseThrow(UserDto::unexpectedException))
            .collect(Collectors.toList());
    }

    /*This exception should not happen as a RoleDb should always convert to a RoleDto */
    private static <T> IllegalStateException unexpectedException(Failure<T> failure) {
        logger.error(ERROR_DUE_TO_INVALID_ROLE);
        throw new IllegalStateException(failure.getException());
    }

    private List<RoleDb> createRoleDb() {
        return
            Optional.ofNullable(this.roles)
                .stream()
                .flatMap(Collection::stream)
                .map(attempt(RoleDto::toRoleDb))
                .map(attempt -> attempt.orElseThrow(UserDto::unexpectedException))
                .collect(Collectors.toList());
    }

    public static final class Builder {

        private String username;
        private String institution;
        private List<RoleDto> roles;

        private Builder() {
            roles = Collections.emptyList();
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withInstitution(String institution) {
            this.institution = institution;
            return this;
        }

        public Builder withRoles(List<RoleDto> roles) {
            this.roles = roles;
            return this;
        }

        /**
         * creates a UserDto instance.
         *
         * @return a {@link UserDto}
         * @throws InvalidEntryInternalException when the used to be built is invalid.
         */
        public UserDto build() throws InvalidEntryInternalException {
            if (StringUtils.isEmpty(username)) {
                throw new InvalidEntryInternalException(MISSING_FIELD_ERROR + "username");
            }
            return new UserDto(this);
        }
    }
}
