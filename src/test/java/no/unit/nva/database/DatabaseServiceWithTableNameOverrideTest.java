package no.unit.nva.database;

import static no.unit.nva.database.DatabaseServiceWithTableNameOverride.DYNAMO_DB_CLIENT_NOT_SET_ERROR;
import static no.unit.nva.database.DatabaseServiceWithTableNameOverride.createMapperOverridingHardCodedTableName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import no.unit.nva.database.interfaces.WithEnvironment;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class DatabaseServiceWithTableNameOverrideTest implements WithEnvironment {

    @Test
    void createMapperOverridingHardCodedTableNameThrowsExceptionWhenDynamoClientIsNull() {
        Executable action =
            () -> createMapperOverridingHardCodedTableName(null, mockEnvironment());
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), instanceOf(NullPointerException.class));
    }

    @Test
    void createMapperOverridingHardCodedTableNameLogsErrorSayingThatMapperIsNull() {
        TestAppender appender = LogUtils.getTestingAppender(DatabaseServiceWithTableNameOverride.class);
        Executable action =
            () -> createMapperOverridingHardCodedTableName(null, mockEnvironment());
        assertThrows(RuntimeException.class, action);
        assertThat(appender.getMessages(), containsString(DYNAMO_DB_CLIENT_NOT_SET_ERROR));
    }
}