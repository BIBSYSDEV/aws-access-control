package no.unit.nva.database.interfaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import no.unit.nva.database.RoleDb;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import org.junit.jupiter.api.Test;

class DynamoEntryTest {

    public static final String SOME_TYPE = "SomeType";

    @Test
    void setTypeHasNoEffect() throws InvalidEntryInternalException {
        RoleDb roleDb = RoleDb.newBuilder().withName("SomeName").build();
        RoleDb copy = roleDb.copy().build();
        copy.setType(SOME_TYPE);

        assertThat(copy, is(equalTo(roleDb)));
    }

    @Test
    void setPrimaryRangeKey() throws InvalidEntryInternalException {
        RoleDb roleDb = RoleDb.newBuilder().withName("SomeName").build();
        RoleDb copy = roleDb.copy().build();
        copy.setPrimaryRangeKey(SOME_TYPE);
        assertThat(copy, is(equalTo(roleDb)));
    }
}