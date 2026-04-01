Feature: ptcadapter

  Background:
    * def requestData = read(requestXml)
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: ptcadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/getPSIDForEINResponse/PSID == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: ptcadapterVIP
    Given url data.VIPURL
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/getPSIDForEINResponse/PSID == data.Response