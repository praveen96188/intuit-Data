Feature: Authorization Header
  @ignore
  Scenario: Authorization Header
    * def realmId = '50000003'
    * def authorization = call read('classpath:com/intuit/psp/karate/utils/headers/authorization.feature') {"username":"#(testUser)","password":"#(testPassword)","realmId":#(realmId),"appId":#(appId),"secret":#(secret)}
    * def header = authorization.header

