Feature: ewsadapter

  Background:
    * def requestData = read(requestXml)
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: ewsadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
#    todo match pre-prod responses correctly
#    And match response/Envelope/Body/Query_AccountResponse/QueryAccountResponse/PSID == data.Response
    And match response/Envelope/Body/Query_AccountResponse/QueryAccountResponse/ResponseStatus/Message == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: ewsadapterVIP
    Given url data.VIPURL
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/Query_AccountResponse/QueryAccountResponse/ResponseStatus/Message == data.Response