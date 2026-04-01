Feature: cdmadapterv4

  Background:
    * def requestData = read(requestJson)
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: cdmadapterv4Gateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "application/json"
    And request requestData
    When method post
    Then status 200
    And match response[0].data[0].employers[0].employerName == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: cdmadapterv4VIP
    Given url data.VIPURL
    And header authorization = authHeader
    And header Content-Type = "application/json"
    And request requestData
    When method post
    Then status 200
    And match response[0].data[0].employers[0].employerName == data.Response