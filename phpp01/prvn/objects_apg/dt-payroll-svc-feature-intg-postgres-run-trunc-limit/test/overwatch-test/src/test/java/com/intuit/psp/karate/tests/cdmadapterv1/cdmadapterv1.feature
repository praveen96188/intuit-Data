Feature: cdmadapterv1

  Background:
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: cdmadapterv1Gateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Accept = "application/json"
    When method get
    Then status 200
    And match response.QueryResp[0].PayrollEmployee[0].FullName == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: cdmadapterv1VIP
    Given url data.VIPURL
    And header Accept = "application/json"
    When method get
    Then status 200
    And match response.QueryResp[0].PayrollEmployee[0].FullName == data.Response