Feature: Core API
  @android_integration
  Scenario Outline: adding a pupil adds name to list and saves name
    Given I am in a classroom
     When I add a pupil to the classroom
     Then the pupil name is added to the list
     And saved in the database