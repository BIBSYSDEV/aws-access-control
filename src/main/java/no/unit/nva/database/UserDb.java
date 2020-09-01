package no.unit.nva.database;

import static java.util.Objects.isNull;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_HASH_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_RANGE_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.SEARCH_USERS_BY_INSTITUTION_INDEX_NAME;
import static no.unit.nva.database.DatabaseIndexDetails.SECONDARY_INDEX_1_HASH_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.SECONDARY_INDEX_1_RANGE_KEY;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import no.unit.nva.database.UserDb.Builder;
import no.unit.nva.database.interfaces.DynamoEntry;
import no.unit.nva.database.interfaces.WithCopy;
import no.unit.nva.database.interfaces.WithType;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import nva.commons.utils.JacocoGenerated;

@DynamoDBTable(tableName = "OverridenByEnvironmentVariable")
public class UserDb extends DynamoEntry implements WithCopy<Builder>, WithType {

    public static final String TYPE = "USER";
    public static final String INVALID_USER_EMPTY_USERNAME = "Invalid user entry: Empty username is not allowed";
    public static final String INVALID_PRIMARY_HASH_KEY = "PrimaryHashKey of user should start with \"USER\"";

    private String primaryHashKey;
    private String username;
    private String institution;
    private List<RoleDb> roles;

    public UserDb() {
        super();
    }

    private UserDb(Builder builder) throws InvalidEntryInternalException {
        super();
        setUsername(builder.username);
        setInstitution(builder.institution);
        setRoles(builder.roles);
        setPrimaryHashKey(builder.primaryHashKey);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JacocoGenerated
    @DynamoDBHashKey(attributeName = PRIMARY_KEY_HASH_KEY)
    @Override
    public String getPrimaryHashKey() {
        return this.primaryHashKey;
    }

    /**
     * Do not use this function. This function is defined only for internal usage (by DynamoDB). The function does not
     * reset the primaryKey once it has been set. It does not throw an Exception because this method is supposed ot be
     * used only by DynamoDb. For any other purpose use the {@link UserDb.Builder}
     *
     * @param primaryKey the primaryKey
     * @throws InvalidEntryInternalException when the primary key is invalid.
     */
    public void setPrimaryHashKey(String primaryKey) throws InvalidEntryInternalException {
        if (primaryKeyHasNotBeenSet()) {
            if (!primaryKey.startsWith(TYPE)) {
                throw new InvalidEntryInternalException(INVALID_PRIMARY_HASH_KEY);
            }
            this.primaryHashKey = primaryKey;
        }
    }

    @JacocoGenerated
    @DynamoDBRangeKey(attributeName = PRIMARY_KEY_RANGE_KEY)
    @Override
    public String getPrimaryRangeKey() {
        return getType();
    }

    @JacocoGenerated
    @DynamoDBIndexHashKey(attributeName = SECONDARY_INDEX_1_HASH_KEY,
        globalSecondaryIndexName = SEARCH_USERS_BY_INSTITUTION_INDEX_NAME)
    public String getSearchByInstitutionHashKey() {
        return this.getInstitution();
    }

    @JacocoGenerated
    public void setSearchByInstitutionHashKey(String searchByInstitutionHashKey) {
        //DO NOTHING
    }

    @JacocoGenerated
    @DynamoDBIndexRangeKey(attributeName = SECONDARY_INDEX_1_RANGE_KEY, globalSecondaryIndexName =
        SEARCH_USERS_BY_INSTITUTION_INDEX_NAME)
    public String getSearchByInstitutionRangeKey() {
        return this.getUsername();
    }

    @JacocoGenerated
    public void setSearchByInstitutionRangeKey(String searchByInstitutionRangeKey) {
        //DO NOTHING
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "type")
    @Override
    public String getType() {
        return TYPE;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "username")
    public String getUsername() {
        return username;
    }

    /**
     * Method for using only for DynamoDb mapper. Do not use. Use the builder instead.
     *
     * @param username the username of the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "roles")
    public List<RoleDb> getRoles() {
        return roles;
    }

    /**
     * Method for using only for DynamoDb mapper. Do not use. Use the builder instead.
     *
     * @param roles the roles.
     */
    public void setRoles(List<RoleDb> roles) {
        this.roles = roles;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "institution")
    public String getInstitution() {
        return institution;
    }

    /**
     * Method for using only for DynamoDb mapper. Do not use. Use the builder instead.
     *
     * @param institution the institution.
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    @Override
    public UserDb.Builder copy() {
        return newBuilder()
            .withUsername(this.username)
            .withInstitution(this.institution)
            .withRoles(this.roles);
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
        UserDb userDb = (UserDb) o;
        return Objects.equals(getPrimaryHashKey(), userDb.getPrimaryHashKey())
            && Objects.equals(getPrimaryRangeKey(), userDb.getPrimaryRangeKey())
            && Objects.equals(getSearchByInstitutionHashKey(), userDb.getSearchByInstitutionHashKey())
            && Objects.equals(getSearchByInstitutionRangeKey(), userDb.getSearchByInstitutionRangeKey())
            && Objects.equals(getUsername(), userDb.getUsername())
            && Objects.equals(getInstitution(), userDb.getInstitution())
            && Objects.equals(getRoles(), userDb.getRoles());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getPrimaryHashKey(), getUsername(), getInstitution(), getRoles());
    }

    public static final class Builder {

        private String username;
        private String institution;
        private List<RoleDb> roles;
        private String primaryHashKey;

        private Builder() {
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withInstitution(String institution) {
            this.institution = institution;
            return this;
        }

        public Builder withRoles(Collection<RoleDb> roles) {
            this.roles = new ArrayList<>(roles);
            return this;
        }

        public UserDb build() throws InvalidEntryInternalException {
            this.primaryHashKey = formatPrimaryHashKey();
            return new UserDb(this);
        }

        private String formatPrimaryHashKey() throws InvalidEntryInternalException {
            if (isNull(username) || username.isBlank()) {
                throw new InvalidEntryInternalException(INVALID_USER_EMPTY_USERNAME);
            } else {
                return String.join(FIELD_DELIMITER, TYPE, username);
            }
        }
    }
}
