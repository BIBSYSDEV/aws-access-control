package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static no.unit.nva.model.DoesNotHaveNullFields.doesNotHaveNullFields;
import static no.unit.nva.utils.EntityUtils.createRole;
import static no.unit.nva.utils.EntityUtils.createUserWithoutUsername;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DatabaseServiceTest extends DatabaseAccessor {

    private static final String SOME_USERNAME = "someusername";
    private static final String SOME_OTHER_USERNAME = "someotherusername";
    private static final String SOME_ROLE = "SomeRole";
    private static final String SOME_INSTITUTION = "SomeInstitution";
    private static final String SOME_OTHER_ROLE = "SOME_OTHER_ROLE";
    private static final String SOME_OTHER_INSTITUTION = "Some other institution";
    private DatabaseService db;

    @BeforeEach
    public void init() {
        db = createDatabaseServiceUsingLocalStorage();
    }

    @Test
    public void databaseServiceHasAMethodForInsertingAUser()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto user = createSampleUserWithoutInstitutionOrRoles(SOME_USERNAME);
        db.addUser(user);
    }

    @DisplayName("getRole() returns non empty role when role-name exists in database")
    @Test
    public void databaseServiceReturnsNonEmptyRoleWhenRoleNameExistsInDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        RoleDto insertedRole = createSampleRoleAndAddToDb(SOME_ROLE);
        RoleDto savedRole = db.getRole(insertedRole);

        assertThat(insertedRole, doesNotHaveNullFields());
        assertThat(savedRole, is(equalTo(insertedRole)));
    }

    @DisplayName("getRole() throws NotFoundException when the role-name does not exist in the database")
    @Test
    public void databaseServiceThrowsNotFoundExceptionWhenRoleNameDoesNotExist() throws InvalidEntryInternalException {
        RoleDto queryObject = createRole(SOME_ROLE);
        Executable action = () -> db.getRole(queryObject);

        NotFoundException exception = assertThrows(NotFoundException.class, action);
        assertThat(exception.getMessage(), containsString(DatabaseServiceImpl.ROLE_NOT_FOUND_MESSAGE));
    }

    @DisplayName("addRole() inserts valid role")
    @Test
    public void addRoleInsertsValidItemInDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        RoleDto insertedUser = createSampleRoleAndAddToDb(SOME_ROLE);
        RoleDto savedUser = db.getRole(insertedUser);

        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @DisplayName("addRole() throws Exception when trying to save role without name")
    @Test
    public void addRoleShouldNotSaveUserWithoutUsername() throws InvalidEntryInternalException {
        RoleDto illegalRole = createIllegalRole();
        Executable illegalAction = () -> db.addRole(illegalRole);
        InvalidInputException exception = assertThrows(InvalidInputException.class, illegalAction);
        assertThat(exception.getMessage(), containsString(RoleDto.INVALID_ROLE_ERROR_MESSAGE));
    }

    @DisplayName("addRole() throws ConflictException when trying to save user with existing username")
    @Test
    public void addRoleThrowsConflictExceptionWhenTryingToSaveAlreadyExistingUser()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException {

        String conflictingRoleName = SOME_ROLE;
        createSampleRoleAndAddToDb(conflictingRoleName);

        RoleDto conflictingRole = createRole(conflictingRoleName);

        Executable action = () -> db.addRole(conflictingRole);
        ConflictException exception = assertThrows(ConflictException.class, action);
        assertThat(exception.getMessage(), containsString(DatabaseServiceImpl.ROLE_ALREADY_EXISTS_ERROR_MESSAGE));
    }

    @DisplayName("getUser() returns non empty user when username exists in database")
    @Test
    public void databaseServiceReturnsNonEmptyUserWhenUsernameExistsInDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto savedUser = db.getUser(insertedUser);

        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @DisplayName("getUser() throws NotFoundException when the username does exist in the database")
    @Test
    public void databaseServiceThrowsNotFoundExceptionWhenUsernameDoesNotExist() throws InvalidEntryInternalException {
        UserDto queryObject = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        Executable action = () -> db.getUser(queryObject);

        NotFoundException exception = assertThrows(NotFoundException.class, action);
        assertThat(exception.getMessage(), containsString(DatabaseServiceImpl.USER_NOT_FOUND_MESSAGE));
    }

    @DisplayName("addUser() inserts valid user with institution and roles in database")
    @Test
    public void addUserInsertsValidItemInDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto savedUser = db.getUser(insertedUser);

        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @DisplayName("addUser() saves user with roles and without institution")
    @Test
    public void addUserSavesAUserWithoutInstitution() throws InvalidEntryInternalException, ConflictException,
                                                             InvalidInputException, NotFoundException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, null, SOME_ROLE);
        UserDto actualUser = db.getUser(expectedUser);

        assertThat(actualUser, is(equalTo(expectedUser)));
        assertThat(actualUser.getInstitution(), is(equalTo(null)));
    }

    @DisplayName("addUser() saves user with institution without roles")
    @Test
    public void addUserShouldSaveUserWithoutRoles()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, null);
        UserDto actualUser = db.getUser(expectedUser);

        assertThat(actualUser, is(equalTo(expectedUser)));
    }

    @DisplayName("addUser() throws Exception when trying to save user without username")
    @Test
    public void addUserShouldNotSaveUserWithoutUsername() {
        Executable illegalAction = () -> db.addUser(createUserWithoutUsername());
        InvalidInputException exception = assertThrows(InvalidInputException.class, illegalAction);
        assertThat(exception.getClass(), is(equalTo(InvalidInputException.class)));
        assertThat(exception.getMessage(), containsString(UserDto.INVALID_USER_ERROR_MESSAGE));
    }

    @DisplayName("addUser() throws ConflictException when trying to save user with existing username")
    @Test
    public void addUserThrowsConflictExceptionWhenTryingToSaveAlreadyExistingUser()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException {

        String conflictingUsername = SOME_USERNAME;
        createSampleUserAndAddUserToDb(conflictingUsername, SOME_INSTITUTION, SOME_ROLE);

        UserDto conflictingUser = UserDto.newBuilder().withUsername(conflictingUsername)
            .withInstitution(SOME_OTHER_INSTITUTION)
            .withRoles(Collections.singletonList(createRole(SOME_OTHER_ROLE)))
            .build();

        Executable action = () -> db.addUser(conflictingUser);
        ConflictException exception = assertThrows(ConflictException.class, action);
        assertThat(exception.getMessage(), containsString(DatabaseServiceImpl.USER_ALREADY_EXISTS_ERROR_MESSAGE));
    }

    @DisplayName("updateUser() updates existing user with input user when input user is valid")
    @Test
    public void updateUserUpdatesExistingUserWithInputUserWhenInputUserIsValid()
        throws ConflictException, InvalidEntryInternalException, NotFoundException, InvalidInputException {
        UserDto existingUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto expectedUser = cloneAndChangeRole(existingUser);

        db.updateUser(expectedUser);
        UserDto actualUser = db.getUser(expectedUser);
        assertThat(actualUser, is(equalTo(expectedUser)));
        assertThat(actualUser, is(not(sameInstance(expectedUser))));
    }

    @DisplayName("updateUser() throws NotFoundException when the input username does not exist")
    @Test
    public void updateUserThrowsNotFoundExceptionWhenTheInputUsernameDoesNotExist()
        throws InvalidEntryInternalException {
        UserDto userUpdate = createSampleUser(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        Executable action = () -> db.updateUser(userUpdate);
        NotFoundException exception = assertThrows(NotFoundException.class, action);
        assertThat(exception.getMessage(), containsString(DatabaseServiceImpl.USER_NOT_FOUND_MESSAGE));
    }

    @DisplayName("updateUser() throws InvalidInputException when the input is invalid ")
    @Test
    public void updateUserThrowsInvalidInputExceptionWhenTheInputisInvalid()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException, NoSuchMethodException,
               IllegalAccessException, InvocationTargetException {
        createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto invalidUser = createUserWithoutUsername();
        Executable action = () -> db.updateUser(invalidUser);
        InvalidInputException exception = assertThrows(InvalidInputException.class, action);
        assertThat(exception.getMessage(), containsString(UserDto.INVALID_USER_ERROR_MESSAGE));
    }

    @Test
    public void listUsersByInstitutionReturnsAllUsersForSpecifiedInstitution()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        UserDto someUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto someOtherUser = createSampleUserAndAddUserToDb(SOME_OTHER_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        List<UserDto> queryResult = db.listUsers(SOME_INSTITUTION);
        assertThat(queryResult, containsInAnyOrder(someUser, someOtherUser));
    }

    @Test
    public void listUsersByInstitutionReturnsEmptyListWhenThereAreNoUsersForSpecifiedInstitution()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        List<UserDto> queryResult = db.listUsers(SOME_OTHER_INSTITUTION);
        assertThat(queryResult, is(empty()));
    }

    private UserDto createSampleUserWithoutInstitutionOrRoles(String username) throws InvalidEntryInternalException {
        return createSampleUser(username, null, null);
    }

    private RoleDto createIllegalRole() throws InvalidEntryInternalException {
        RoleDto illegalRole = createRole(SOME_ROLE);
        illegalRole.setRoleName(null);
        return illegalRole;
    }

    private UserDto cloneAndChangeRole(UserDto existingUser) throws InvalidEntryInternalException {
        RoleDto someOtherRole = createRole(SOME_OTHER_ROLE);
        return existingUser.copy().withRoles(Collections.singletonList(someOtherRole)).build();
    }

    private UserDto createSampleUserAndAddUserToDb(String username, String institution, String roleName)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto userDto = createSampleUser(username, institution, roleName);
        db.addUser(userDto);
        return userDto;
    }

    private UserDto createSampleUser(String username, String institution, String roleName)
        throws InvalidEntryInternalException {
        return UserDto.newBuilder()
            .withRoles(createRoleList(roleName))
            .withInstitution(institution)
            .withUsername(username)
            .build();
    }

    private RoleDto createSampleRoleAndAddToDb(String roleName)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        RoleDto roleDto = createRole(roleName);
        db.addRole(roleDto);
        return roleDto;
    }

    private List<RoleDto> createRoleList(String rolename) throws InvalidEntryInternalException {
        if (nonNull(rolename)) {
            RoleDto roleDto = createRole(rolename);
            return Collections.singletonList(roleDto);
        } else {
            return Collections.emptyList();
        }
    }
}
