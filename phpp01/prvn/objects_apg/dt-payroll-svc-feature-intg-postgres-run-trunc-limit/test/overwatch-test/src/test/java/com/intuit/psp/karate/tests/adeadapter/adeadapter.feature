Feature: adeadapter

  Background:
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: adeadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Accept = "application/json"
    When method get
    Then status 200
    And match response.Name == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: adeadapterVIP
    Given url data.VIPURL
    And header Accept = "application/json"
    When method get
    Then status 200
    And match response.Name == data.Response
