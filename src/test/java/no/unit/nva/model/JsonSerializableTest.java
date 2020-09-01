package no.unit.nva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class JsonSerializableTest {

    public static final String MOCK_JSON_STRING = "SomeString";

    @Test
    public void toJsonStringReturnsATheStringOfTheSerializer() throws JsonProcessingException {
        ObjectMapper jsonParser = createJsonParserThatReturnsAString();
        JsonSerializable serializable = new JsonSerializable() {};
        String jsonString = serializable.toJsonString(jsonParser);
        assertThat(jsonString, is(equalTo(MOCK_JSON_STRING)));
    }

    @Test
    public void toJsonStringThrowsRuntimeExceptionContainingTheCauseWhenSerializingThrowsException()
        throws JsonProcessingException {
        ObjectMapper jsonParser = createJsonParserThrowingException();
        JsonSerializable serializable = new JsonSerializable() {};
        Executable action = () -> serializable.toJsonString(jsonParser);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        Throwable cause = exception.getCause();
        assertThat(cause, is(instanceOf(JsonParseException.class)));
    }

    private ObjectMapper createJsonParserThrowingException() throws JsonProcessingException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any(JsonSerializable.class)))
            .thenThrow(JsonParseException.class);
        return objectMapper;
    }

    private ObjectMapper createJsonParserThatReturnsAString() throws JsonProcessingException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any(JsonSerializable.class)))
            .thenReturn(MOCK_JSON_STRING);
        return objectMapper;
    }
}