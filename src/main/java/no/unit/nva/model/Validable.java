package no.unit.nva.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.unit.nva.exceptions.InvalidInputException;

public interface Validable {

    @JsonIgnore
    boolean isValid();

    InvalidInputException exceptionWhenInvalid();
}
