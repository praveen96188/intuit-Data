Feature: qbdtadapter

  Background:
    * def requestData = read(requestXml)
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: qbdtadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "application/xml"
    And request requestData
    When method post
    Then status 200
    And match response contains data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: qbdtadapterVIP
    Given url data.VIPURL
    And header Content-Type = "application/xml"
    And request requestData
    When method post
    Then status 200
    And match response contains data.Response