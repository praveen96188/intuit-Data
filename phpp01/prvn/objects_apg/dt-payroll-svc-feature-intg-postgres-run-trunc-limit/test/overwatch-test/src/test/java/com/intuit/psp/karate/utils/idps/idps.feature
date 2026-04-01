Feature: IDPS secret
  @ignore
  Scenario: IDPS secret
    * def idpsConfig = karate.read("classpath:idps-config.json");
    * def IdpsClientFactory = karate.read("this:idpsClient_"+env+".js");
    * def idpsClient = IdpsClientFactory(idpsConfig[env]);
    And print idpsClient
    * def globalAll = karate.read("classpath:global-config.json");
    * def globalConfig = globalAll[env]
    And print globalConfig
    * def envAll = karate.read("classpath:env-config.json");
    And print envAll
    * def envConfig = envAll[env];
    And print envConfig
    * def bool1 = idpsClient.resolveSecrets(globalConfig);
    And print bool1
    * def bool2 = idpsClient.resolveSecrets(envConfig);
    And print bool2
