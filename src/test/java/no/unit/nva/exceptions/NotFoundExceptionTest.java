package no.unit.nva.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class NotFoundExceptionTest {

    public static final String SOME_MESSAGE = "Some message";

    @Test
    public void exceptionReturnsNotFoundStatusCode() {
        NotFoundException exception = new NotFoundException(SOME_MESSAGE);
        assertThat(exception.getStatusCode(), is(equalTo(HttpStatus.SC_NOT_FOUND)));
    }
}