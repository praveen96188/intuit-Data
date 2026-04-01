function IdpsClient(connectionConf) {
    var Properties = Java.type('java.util.Properties');
    var IdpsClient = Java.type('com.intuit.idps.IdpsClient');
    var PropertiesNames = Java.type('com.intuit.idps.service.rest.IdpsProperties.PropertiesNames');

    var SECRET_NAME_PREFIX = '{secret}idps:/';

    // Set properties
    var props = new Properties();
    props.setProperty(PropertiesNames.ENDPOINT.getName(), connectionConf.endpoint);
    props.setProperty(PropertiesNames.API_SECRET_KEY.getName(), karate.properties['user.dir'] + '/' + connectionConf.apiSecretKey);
    props.setProperty(PropertiesNames.API_KEY_ID.getName(), connectionConf.apiKeyId);


    // Create client
    var idpsClient = IdpsClient.Factory.newInstance(props);

    /**
     * Resolve string property values that match IDPS key format.
     *
     * @param obj the object to resolve IDPS secrets in place for
     */
    var resolveSecrets = function (obj) {
        for (var key in obj) {
            var value = obj[key];
            if (value.startsWith(SECRET_NAME_PREFIX)) {
                var keyToResolve = value.slice(SECRET_NAME_PREFIX.length);
                obj[key] = idpsClient.getSecretLatest(keyToResolve).getStringValue();
            }
        }
        return true;
    };



    return {
        resolveSecrets: resolveSecrets
    };
}