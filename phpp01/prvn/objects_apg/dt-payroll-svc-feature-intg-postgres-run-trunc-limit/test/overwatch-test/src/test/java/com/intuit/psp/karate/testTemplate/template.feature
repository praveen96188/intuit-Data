Feature: template

  Background:
#    The following line will read the data json file based on env (qal->data_qal.json OR e2e->data_e2e.json)
#    The dataJson variable getting configured in karate-config.js
    * def data = read(dataJson)
#    The following line will read the request file based on env (qal->request_qal.json OR e2e->request_e2e.json)
#    The requestJson(request_<env>.json) variable getting configured in karate-config.js
#    along with requestJson you can also use requestXml(request_<env>.xml) and requestText(request_<env>.txt)
    * def requestData = read(requestJson)
#    The following line will read the response file based on env (qal->response_qal.json OR e2e->response_e2e.json)
#    The responseJson(response_<env>.json) variable getting configured in karate-config.js
#    along with responseJson you can also use responseXml(request_<env>.xml) and responseText(request_<env>.txt)

    * def responseData = read(responseJson)
#    To replace the values in request and response please refer to the link below
#    https://github.com/intuit/karate/blob/master/karate-junit4/src/test/java/com/intuit/karate/junit4/demos/replace.feature

#  To add the which env the testcase can run
  @env=qal,e2e
  Scenario: template
#    baseUrl is configured in karate-config.js
    Given url baseUrl + '/v2/wc-consent-info-details'
#    you can add you headers or create a js function refer to the link https://intuit.github.io/karate/#header
#    authHeader is configured in karate-config.js. If you need different authorization header call authorization.feature
    And header authorization = authHeader
    And header Content-Type = "application/json"
    When method get
    Then status 200
#    Checkout the link https://intuit.github.io/karate/#match to understand the matchers and validate response
    And match response contains responseData

  