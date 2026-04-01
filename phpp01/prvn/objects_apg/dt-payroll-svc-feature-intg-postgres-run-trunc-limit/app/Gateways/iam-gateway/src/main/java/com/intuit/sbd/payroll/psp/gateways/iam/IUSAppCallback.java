package com.intuit.sbd.payroll.psp.gateways.iam;

import com.intuit.client.ius.IUSClientAppCallback;
import com.intuit.client.ius.IamEnvironment;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IUSAppCallback extends IUSClientAppCallback {
    private static ConfigType configType;
    private static final String CONCATENATOR ="_";
    private static final String OFFERING_ID= "offeringid";
    private static final String APP_ID = "appid";
    private static final String APP_SECRET = "appsecret";

    public IUSAppCallback() {
        configType = ConfigType.PSP;
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public String getIUSOfferingId() {
        return getConfigValue(configType, OFFERING_ID);
    }

    @Override
    public String getIUSAppId(String s) {
        return getConfigValue(configType, APP_ID);
    }

    @Override
    public String getIUSAppSecret(String s) {
        return getConfigValue(configType, APP_SECRET);
    }

    @Override
    public String getOfferingRoleNameSpace() {
        return "Intuit.smallbusiness";
    }

    @Override
    public FederatedCredentials getFederatedCredentials() {
        return null;
    }

    /**
     * Return the connection timeout for calls to IUS. If a connection takes longer than this number of milliseconds, the call
     * will time out.  The default is 10 seconds.
     *
     * @return The read timeout for calls to IUS.
     */
    public Integer getIUSConnectionTimeOut() {
        return 5000;
    }

    /**
     * Return the read timeout for calls to IUS. If it takes longer than this number of milliseconds to read the
     * result of a call after connecting, the call will time out.  The default is 10 seconds.
     *
     * @return The read timeout for calls to IUS.
     */
    public Integer getIUSReadTimeOut() {
        return 10000;
    }

    private String getConfigValue(ConfigType configType,String key){
        String derivedKey = StringUtils.join(configType.getValue(),CONCATENATOR, key);
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, derivedKey);
    }

    @Override
    public IamEnvironment getIamEnvironment() {
        if (Application.isProdEnvironment()) {
            return IamEnvironment.PROD;
        } else if(Application.isPerfEnvironment()){
            return IamEnvironment.PERF;
        } else {
            return IamEnvironment.E2E;
        }
    }

}
