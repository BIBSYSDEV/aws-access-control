package no.unit.nva.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class ConflictExceptionTest {

    public static final String SOME_MESSAGE = "Some message";

    @Test
    public void conflictExceptionReturnsConflictStatus() {
        ConflictException exception = new ConflictException(SOME_MESSAGE);
        assertThat(exception.statusCode(), is(equalTo(HttpStatus.SC_CONFLICT)));
    }

    @Test
    public void conflictExceptionMessageContainsProvidedMessage() {
        ConflictException exception = new ConflictException(SOME_MESSAGE);
        assertThat(exception.getMessage(), containsString(SOME_MESSAGE));
    }
}