function fn() {
    var env = karate.env; // get system property 'karate.env'

    if (!env) {
        env = karate.properties['spring.profile.active'];
        if (!env) {
            karate.log('karate.env config not found. Setting env:qal');
            env = 'qal';
        }
    }
    var config = {
        env: env
    }

    var idps;
    if(env != 'prd' && env != 'east_prd') {
        idps = karate.callSingle('classpath:com/intuit/psp/karate/utils/idps/idps.feature', config);
        updateConfig(idps.globalConfig, config);
        updateConfig(idps.envConfig, config);
    } else {
        var evars= java.lang.System.getenv();
        config.appId =  evars.TEST_CLIENT_APP_SECRET_USR;
        config.secret = evars.TEST_CLIENT_APP_SECRET_PSW;
        config.testUser = evars.SANITY_USER_CREDENTIALS_USR;
        config.testPassword = evars.SANITY_USER_CREDENTIALS_PSW;
    }

    var authHeader = karate.callSingle('classpath:com/intuit/psp/karate/utils/headers/defaultAuthorization.feature', config);
    var regionIndependantEnv = env.replace("east_","");
    config.authHeader = authHeader.header
    config.requestJson = "this:request_"+regionIndependantEnv+".json";
    config.responseJson = "this:response_"+regionIndependantEnv+".json";
    config.requestText = "this:request_"+regionIndependantEnv+".txt";
    config.responseText = "this:response_"+regionIndependantEnv+".txt";
    config.requestXml = "this:request_"+regionIndependantEnv+".xml";
    config.responseXml = "this:response_"+regionIndependantEnv+".xml";

    //Since data contain region dependant urls so use region dependant data
    config.dataJson = "this:data_"+env+".json";


    function updateConfig(envConfig, config) {
        for (var key in envConfig) {
            config[key] = envConfig[key]
        }
    }
    return config;
}



