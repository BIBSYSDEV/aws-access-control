package no.unit.nva.model;

import static nva.commons.utils.attempt.Try.attempt;

import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.utils.attempt.Failure;

public interface JsonSerializable {

    /**
     * a JSON representation of the object.
     *
     * @return a JSON representation of the object.
     */
    default String toJsonString(ObjectMapper objectMapper) {
        return
            attempt(() -> objectMapper.writeValueAsString(this))
                .orElseThrow(this::newUnexpectedException);
    }

    private RuntimeException newUnexpectedException(Failure<String> fail) {
        throw new RuntimeException(fail.getException());
    }
}
