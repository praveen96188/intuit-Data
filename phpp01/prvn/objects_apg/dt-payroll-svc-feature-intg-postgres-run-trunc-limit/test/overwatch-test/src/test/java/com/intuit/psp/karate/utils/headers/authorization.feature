Feature: Authorization Header
  @ignore
  Scenario: Authorization Header
    * def realmId = '50000003'
    * def auth = call read('classpath:com/intuit/psp/karate/utils/ticket/userSignIn.feature') {"username":"#(__arg.username)","password":"#(__arg.password)", "appId":"#(__arg.appId)", "secret":"#(__arg.secret)"}
    * def header = 'Intuit_IAM_Authentication intuit_appid=<appId>,intuit_app_secret=<secret>,intuit_token_type=IAM-Ticket,intuit_realmid=<realmId>,intuit_token=<ticket>,intuit_userid=<authId>'
    * replace header
      | token     | value             |
      | realmId   | __arg.realmId     |
      | appId     | __arg.appId       |
      | secret    | __arg.secret      |
      | authId    | auth.authId       |
      | ticket    | auth.ticket       |

