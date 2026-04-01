Feature: qbdtwsadapter

  Background:
    * def requestData = read(requestXml)
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: qbdtwsadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And print response
    And match response/Envelope/Body/QueryPaymentStatusResponse/return/ProcessingMessages/ProcessingMessage/Message == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: qbdtwsadapterVIP
    Given url data.VIPURL
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And print response
    And match response/Envelope/Body/QueryPaymentStatusResponse/return/ProcessingMessages/ProcessingMessage/Message == data.Response

  