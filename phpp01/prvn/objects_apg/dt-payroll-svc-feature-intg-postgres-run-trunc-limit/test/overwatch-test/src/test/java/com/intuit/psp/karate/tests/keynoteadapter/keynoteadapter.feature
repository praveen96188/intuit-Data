Feature: keynoteadapter

  Background:
    * def requestData = read(requestXml)
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: keynoteadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/KeynoteValidationResponse/return/ProcessingMessages/ProcessingMessage/Message == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: keynoteadapterVIP
    Given url data.VIPURL
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/KeynoteValidationResponse/return/ProcessingMessages/ProcessingMessage/Message == data.Response