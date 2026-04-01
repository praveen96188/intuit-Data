package com.intuit.sbd.payroll.psp.gateways.salestax.offlineticket;

import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequest;
import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequestBuilder;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.gateways.salestax.iam.AuthorizationBuilder;
import org.apache.commons.lang3.StringUtils;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is duplicated to avoid cyclic dependency utils -> payroll-services-api -> domain -> salestax-gateway -> utils. This class is not to be
 * used by other modules to generate offline ticket. Please use the original offline ticket generator present under Common utils.
 */
public class OfflineTicketGenerator {

    private static final String APP_ID = "appid";
    private static final String APP_SECRET = "appsecret";
    private static final String ASSET_ID = "assetid";
    private static final String OFFERING_ID= "offeringid";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String AUDIENCES = "audiences";
    private static final String IAMURL = "offlineticket_external_endpoint";
    private static final String CONCATENATOR ="_";

    private static OfflineTicketGenerator instance;
    private Map<ConfigType,AuthorizationBuilder> offlineTicketConfigMap;
    private static final Lock lockObj=new ReentrantLock();

    private OfflineTicketGenerator(){
        offlineTicketConfigMap = new ConcurrentHashMap<ConfigType,AuthorizationBuilder>();
    }

    public static OfflineTicketGenerator getInstance(){
        if(instance==null){
            lockObj.lock();
            if(instance==null){
                instance=new OfflineTicketGenerator();
            }
            lockObj.unlock();
        }
        return instance;
    }


    public  String getOfflineTicket(ConfigType configType){
        AuthorizationBuilder authorizationBuilder = getAuthorizationBuilder(configType);
        return authorizationBuilder.buildAuthorizationHeaderWithOfflineTicket();
    }

    private AuthorizationBuilder createAuthorizationBuilder(ConfigType configType){
        GetAuthHeaderForSystemOfflineTicketRequest authHeaderForSystemOfflineTicketRequest = new GetAuthHeaderForSystemOfflineTicketRequestBuilder()
                .setAppId(getConfigValue(configType, APP_ID))
                .setOfferingId(getConfigValue(configType, OFFERING_ID))
                .setAppSecret(getConfigValue(configType, APP_SECRET))
                .setUsername(getConfigValue(configType, USERNAME))
                .setPassword(getConfigValue(configType, PASSWORD)).setAssetId(getConfigValue(configType, ASSET_ID))
                .setAudiences(Collections.singletonList(getConfigValue(configType, AUDIENCES))).setIp(getIpAddress()).build();

        AuthorizationBuilder authorizationBuilder=new AuthorizationBuilder(getConfigValue(configType, IAMURL), authHeaderForSystemOfflineTicketRequest);
        return authorizationBuilder;
    }

    public static String getIpAddress() {
        String ipAddress = null;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }


    private  AuthorizationBuilder getAuthorizationBuilder(ConfigType configType){
        AuthorizationBuilder authorizationBuilder = offlineTicketConfigMap.get(configType);

        if(Objects.nonNull(authorizationBuilder)){
            return authorizationBuilder;
        }
        authorizationBuilder = createAuthorizationBuilder(configType);
        offlineTicketConfigMap.put(configType,authorizationBuilder);
        return authorizationBuilder;

    }

    private String getConfigValue(ConfigType configType,String key){
        String derivedKey = StringUtils.join(configType.getValue(),CONCATENATOR, key);
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, derivedKey);
    }

}

