package no.unit.nva.database;

import static java.util.Objects.isNull;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_HASH_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_RANGE_KEY;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.Objects;
import no.unit.nva.database.interfaces.DynamoEntry;
import no.unit.nva.database.interfaces.WithCopy;
import no.unit.nva.database.interfaces.WithType;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import nva.commons.utils.JacocoGenerated;

@DynamoDBTable(tableName = "OverridenByEnvironmentVariable")
public class RoleDb extends DynamoEntry implements WithCopy<RoleDb.Builder>, WithType {

    private static final String INVALID_PRIMARY_HASH_KEY = "PrimaryHashKey should start with \"ROLE\"";
    public static String TYPE = "ROLE";

    private String primaryHashKey;
    private String name;

    public RoleDb() {
        super();
    }

    private RoleDb(Builder builder) throws InvalidEntryInternalException {
        super();
        setName(builder.name);
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
     * Do not use this method. This is only for usage by the DynamoDbMapper. Sets the hash key value for the database
     * entry. This is the hashKey for the table and not any secondary index.
     *
     * @param primaryHashKey the primary hash key saved in the database
     * @throws InvalidEntryInternalException when the role is invalid.
     */
    @JacocoGenerated
    public void setPrimaryHashKey(String primaryHashKey) throws InvalidEntryInternalException {
        if (primaryKeyHasNotBeenSet()) {
            if (!primaryHashKey.startsWith(TYPE)) {
                throw new InvalidEntryInternalException(INVALID_PRIMARY_HASH_KEY);
            }
            this.primaryHashKey = primaryHashKey;
        }
    }

    @DynamoDBRangeKey(attributeName = PRIMARY_KEY_RANGE_KEY)
    @Override
    public String getPrimaryRangeKey() {
        return getType();
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    @JacocoGenerated
    public void setName(String name) {
        this.name = name;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "type")
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Builder copy() {
        return new Builder().withName(this.name);
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
        RoleDb roleDb = (RoleDb) o;
        return getName().equals(roleDb.getName());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getName());
    }

    public static final class Builder {

        public static final String EMPTY_ROLE_NAME_ERROR = "Rolename cannot be null or blank";
        private String name;
        private String primaryHashKey;

        private Builder() {
        }

        public Builder withName(String val) {
            name = val;
            return this;
        }

        public RoleDb build() throws InvalidEntryInternalException {
            this.primaryHashKey = formatPrimaryHashKey();
            return new RoleDb(this);
        }

        private String formatPrimaryHashKey() throws InvalidEntryInternalException {
            if (isNull(name) || name.isBlank()) {
                throw new InvalidEntryInternalException(EMPTY_ROLE_NAME_ERROR);
            } else {
                return String.join(FIELD_DELIMITER, TYPE, name);
            }
        }
    }
}
