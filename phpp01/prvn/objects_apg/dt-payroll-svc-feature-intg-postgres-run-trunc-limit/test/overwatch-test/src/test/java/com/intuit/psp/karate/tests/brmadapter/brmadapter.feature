Feature: brmadapter

  Background:
    * def requestData = read(requestXml)
    * def data = read(dataJson)
  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: brmadapterGateway
    Given url data.GatewayURL
    And header authorization = authHeader
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/getUsageBillingEmployeeDetailsResponse/GetUsageBillingDetailResponse/NumCompaniesBilled == data.Response

  @env=qal,e2e,prd,east_e2e,east_prd
  Scenario: brmadapterVIP
    Given url data.VIPURL
    And header Content-Type = "text/xml"
    And request requestData
    When method post
    Then status 200
    And match response/Envelope/Body/getUsageBillingEmployeeDetailsResponse/GetUsageBillingDetailResponse/NumCompaniesBilled == data.Response