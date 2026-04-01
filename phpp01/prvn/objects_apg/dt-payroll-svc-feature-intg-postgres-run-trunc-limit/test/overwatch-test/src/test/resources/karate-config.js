function() {

    var env = karate.env;
    karate.log('karate.env system property was:', env);

    if (!env) {
        //if env not specified set to your projects default environment
        env = 'qal';
    }

    var config = {
        //default settings for all environments go here
        //override the appId and Secrets
    };

    if (env == 'local') {
        config.v4GraphqlUrl = 'https://localhost:8443/v4/graphql';
        config.baseUrl = 'https://localhost:8443';
        karate.configure('ssl', { trustAll: 'true' })
    }

    if (env == 'qal') {
        //QAL gateway endpoint will go here  
        // default zone is uswest-2 you can override here 
        config.v4GraphqlUrl = 'https://payroll-dtpayroll-qal-dt-payroll-svc.payroll-sgmnt-ppd.iks.a.intuit.com/v4/graphql';
        config.baseUrl = 'https://payroll-dtpayroll-qal-dt-payroll-svc.payroll-sgmnt-ppd.iks.a.intuit.com';     
    }

    if (env == 'e2e') {
        //E2E gateway endpoint will go here  
        // default zone is uswest-2 you can override ere 
        config.v4GraphqlUrl = 'https://payroll-dtpayroll-e2e-dt-payroll-svc.payroll-sgmnt-ppd.iks.a.intuit.com/v4/graphql';
        config.baseUrl = 'https://payroll-dtpayroll-e2e-dt-payroll-svc.payroll-sgmnt-ppd.iks.a.intuit.com';     

    }
    return config;

}
