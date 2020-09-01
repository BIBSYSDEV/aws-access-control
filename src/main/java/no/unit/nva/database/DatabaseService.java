package no.unit.nva.database;

import java.util.List;
import java.util.Optional;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;

public interface DatabaseService {

    String USERS_AND_ROLES_TABLE_NAME_ENV_VARIABLE = "USERS_AND_ROLES_TABLE";

    UserDto getUser(UserDto queryObject) throws InvalidEntryInternalException, NotFoundException;

    List<UserDto> listUsers(String institutionId) throws InvalidEntryInternalException;

    Optional<UserDto> getUserAsOptional(UserDto queryObject) throws InvalidEntryInternalException;

    void addUser(UserDto user) throws InvalidEntryInternalException, ConflictException, InvalidInputException;

    void addRole(RoleDto roleDto)
        throws ConflictException, InvalidInputException, InvalidEntryInternalException;

    void updateUser(UserDto user)
        throws InvalidEntryInternalException, NotFoundException, InvalidInputException;

    RoleDto getRole(RoleDto input) throws InvalidEntryInternalException, NotFoundException;

    Optional<RoleDto> getRoleAsOptional(RoleDto input) throws InvalidEntryInternalException;
}
