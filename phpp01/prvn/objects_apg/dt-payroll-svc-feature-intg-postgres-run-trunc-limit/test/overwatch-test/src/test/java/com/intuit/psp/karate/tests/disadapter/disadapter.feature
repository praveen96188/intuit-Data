Feature: disadapter

  Background:
    * def requestData = read(requestXml)
    * def data = read(dataJson)
  @env=qal,e2e
  Scenario: disadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/GetAgencyRulesResponse/DISResponse/Agencies/PaymentTemplates/PaymentTemplateId contains data.Response

  @env=qal,e2e,east_e2e,east_prd
  Scenario: disadapterVIP
    Given url data.VIPURL
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/GetAgencyRulesResponse/DISResponse/Agencies/PaymentTemplates/PaymentTemplateId contains data.Response