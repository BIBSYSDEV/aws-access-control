package no.unit.nva.database;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import no.unit.nva.database.RoleDb.Builder;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class RoleDbTest extends DatabaseAccessor {

    public static final String SOME_ROLE_NAME = "someRoleName";
    public static final String SOME_OTHER_RANGE_KEY = "SomeOtherRangeKey";
    public static final String SOME_TYPE = "SomeType";
    private final RoleDb sampleRole = createSampleRole();
    private DynamoDBMapper mapper;

    public RoleDbTest() throws InvalidEntryInternalException {
    }

    @Test
    public void getPrimaryHashKeyReturnsStringContainingTypeRole() {
        assertThat(sampleRole.getPrimaryHashKey(), containsString(RoleDb.TYPE));
    }

    @BeforeEach
    void init() {
        mapper = DatabaseServiceWithTableNameOverride
            .createMapperOverridingHardCodedTableName(initializeTestDatabase(), envWithTableName);
    }

    @Test
    void roleDbHasBuilder() {
        RoleDb.Builder builder = RoleDb.newBuilder();
        assertNotNull(builder);
    }

    @Test
    void roleDbHasRoleName() throws InvalidEntryInternalException {
        RoleDb roleDb = createSampleRole();
        assertThat(roleDb.getName(), is(equalTo(SOME_ROLE_NAME)));
    }

    @Test
    void builderSetsTheRolename() throws InvalidEntryInternalException {
        RoleDb role = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        assertThat(role.getName(), is(equalTo(SOME_ROLE_NAME)));
    }

    @Test
    void buildReturnsObjectWithInitializedPrimaryHashKey() throws InvalidEntryInternalException {
        RoleDb role = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        assertThat(role.getPrimaryHashKey(), is(not(nullValue())));
        assertThat(role.getPrimaryHashKey(), is(not(emptyString())));
    }

    @Test
    void buildWithoutRoleNameShouldThrowException() {
        Executable action = () -> RoleDb.newBuilder().build();
        assertThrows(InvalidEntryInternalException.class, action);
    }

    @Test
    void roleDbRoleNameIsSavedInDatabase() {
        mapper.save(sampleRole);
        RoleDb retrievedRole = mapper.load(RoleDb.class, sampleRole.getPrimaryHashKey());
        assertThat(retrievedRole, is(equalTo(sampleRole)));
    }

    @Test
    void getPrimaryHashKeyReturnsStringContainingRoleName() {
        assertThat(sampleRole.getPrimaryHashKey(), containsString(sampleRole.getName()));
    }

    @Test
    void setPrimaryHashKeyShouldNotChangeTheValueOfAlreadySetPrimaryHashKey() throws InvalidEntryInternalException {
        String someOtherHashKey = "SomeOtherHashKey";
        sampleRole.setPrimaryHashKey(someOtherHashKey);
        assertThat(sampleRole.getPrimaryHashKey(), is(not(equalTo(someOtherHashKey))));
        assertThat(sampleRole.getPrimaryHashKey(), containsString(sampleRole.getName()));
        assertThat(sampleRole.getPrimaryHashKey(), containsString(RoleDb.TYPE));
    }

    @ParameterizedTest(name = "setPrimaryHashKey throws exception when input is:\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", "\r"})
    void setPrimaryHashKeyThrowsExceptionWhenInputIsBlankOrNullString(String blankString) {
        Executable action = () -> RoleDb.newBuilder().withName(blankString).build();
        InvalidEntryInternalException exception = assertThrows(InvalidEntryInternalException.class, action);
        assertThat(exception.getMessage(), containsString(Builder.EMPTY_ROLE_NAME_ERROR));
    }

    @Test
    void setPrimaryRangeKeyHasNoEffect() throws InvalidEntryInternalException {
        RoleDb originalRole = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        RoleDb copy = originalRole.copy().build();
        copy.setPrimaryRangeKey(SOME_OTHER_RANGE_KEY);
        assertThat(originalRole, is(equalTo(copy)));
    }

    @Test
    void setTypeHasNoEffect() throws InvalidEntryInternalException {
        RoleDb originalRole = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        RoleDb copy = originalRole.copy().build();
        copy.setType(SOME_TYPE);
        assertThat(originalRole, is(equalTo(copy)));
    }

    @Test
    void copyReturnsBuilderContainingAllFieldValuesOfOriginalItem() throws InvalidEntryInternalException {
        RoleDb copyRole = sampleRole.copy().build();
        assertThat(copyRole, is(equalTo(sampleRole)));
        assertThat(copyRole, is(not(sameInstance(sampleRole))));
    }

    private RoleDb createSampleRole() throws InvalidEntryInternalException {
        return RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
    }
}