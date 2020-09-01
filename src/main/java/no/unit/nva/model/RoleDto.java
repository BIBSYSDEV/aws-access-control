package no.unit.nva.model;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.interfaces.WithCopy;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.StringUtils;

@JsonTypeName(RoleDto.TYPE)
public class RoleDto implements WithCopy<RoleDto.Builder>, JsonSerializable, Validable, Typed {

    public static final String TYPE = "Role";
    public static final String MISSING_ROLE_NAME_ERROR = "Role should have a name";
    public static final String INVALID_ROLE_ERROR_MESSAGE = "RoleName cannot be null for a role.";
    @JsonProperty("rolename")
    private String roleName;

    public RoleDto() {
    }

    private RoleDto(Builder builder) {
        setRoleName(builder.name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static RoleDto fromRoleDb(RoleDb roleDb) throws InvalidEntryInternalException {
        return newBuilder().withName(roleDb.getName()).build();
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
        RoleDto roleDto = (RoleDto) o;
        return getRoleName().equals(roleDto.getRoleName());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getRoleName());
    }

    public RoleDb toRoleDb() throws InvalidEntryInternalException {
        return RoleDb.newBuilder().withName(this.roleName).build();
    }

    @Override
    public RoleDto.Builder copy() {
        return RoleDto.newBuilder().withName(getRoleName());
    }

    @Override
    public String toString() {
        return toJsonString(JsonUtils.objectMapper);
    }

    @Override
    public boolean isValid() {
        return !(isNull(this.getRoleName()) || this.getRoleName().isBlank());
    }

    @Override
    public InvalidInputException exceptionWhenInvalid() {
        return new InvalidInputException(INVALID_ROLE_ERROR_MESSAGE);
    }

    public static final class Builder {

        private String name;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Builds a RoleDto.
         *
         * @return a RoleDto
         * @throws InvalidEntryInternalException when the generated role would be invalid.
         */
        public RoleDto build() throws InvalidEntryInternalException {
            if (StringUtils.isEmpty(this.name)) {
                throw new InvalidEntryInternalException(MISSING_ROLE_NAME_ERROR);
            }
            return new RoleDto(this);
        }
    }
}
