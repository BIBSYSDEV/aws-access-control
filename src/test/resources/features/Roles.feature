Feature: Role features


  Background:
    Given a Database for users and roles
    And an AuthorizedClient that is authorized through Feide

    And an ExistingRole with role-name "ExistingRole" that exists in the Database
    And a NonExistingRole with role-name "NonExistingRole" that does not exist in the Database
    And a NewRole with role-name "NewRole" that does not yet exist in the Database

#
  Scenario: Authorized client creates Role
    When the AuthorizedClient requests to add the NewRole to the Database
    Then an OK message is returned
    And a RoleDescription is returned

  Scenario: Authorized client reads Role
    When the AuthorizedClient requests to read the ExistingRole
    Then a RoleDescription is returned

  Scenario: Authorized client tries to read non-existent Role
    When the AuthorizedClient requests to read the NonExistingRole
    Then a NotFound message is returned
