Feature: SignIn User

  Background:
    * def data = read('this:data.json')
    #craete authorization header based on the env
    * def authorization_header = 'Intuit_IAM_Authentication intuit_appid=<appId>,intuit_app_secret=<secret>'
    * replace authorization_header
      | token     | value             |
      | appId     | __arg.appId       |
      | secret    | __arg.secret      |
    #craete host header based on env
    * def host_header = (env == 'prd' || env == 'east_prd') ? 'access.platform.intuit.com' : 'access-e2e.platform.intuit.com'

  @ignore
  Scenario: SignIn
    Given url data[env]
    And request {"username":"#(__arg.username)","password":"#(__arg.password)"}
    And header host = host_header
    And header intuit_originatingip = "127.0.0.1"
    And header Authorization = authorization_header
    When method post
    Then status 200
#    And print response.iamTicket.ticket
    * def authId = response.iamTicket.userId
    * def ticket = response.iamTicket.ticket




