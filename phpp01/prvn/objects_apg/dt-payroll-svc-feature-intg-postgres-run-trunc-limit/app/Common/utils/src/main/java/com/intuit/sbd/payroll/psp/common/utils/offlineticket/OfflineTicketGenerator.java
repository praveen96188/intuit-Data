package com.intuit.sbd.payroll.psp.common.utils.offlineticket;

import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequest;
import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequestBuilder;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.iam.AuthorizationBuilder;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: mcg
 * Date: 08/10/2018
 * Time: 2:34 PM
 * /**
 *  <p>Class created to get offlinetoken for all AWS endpoints
 *  going through gateway with private auth plus</p>
 *
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

    private SpcfLogger logger = SpcfLogManager.getLogger(OfflineTicketGenerator.class);

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


    public String getOfflineTicket(ConfigType configType, String userContextRealmId, String targetRealmId){
        //Client initialization in Identity1
        AuthorizationBuilder authorizationBuilder = getAuthorizationBuilder(configType);
        //Get Auth Headers with Identity 1
        GetAuthHeaderForSystemOfflineTicketRequest getAuthHeaderForSystemOfflineTicketRequest
                = getGetAuthHeaderForSystemOfflineTicketRequest(configType, userContextRealmId, targetRealmId);
        return authorizationBuilder.buildAuthorizationHeaderWithOfflineTicket(getAuthHeaderForSystemOfflineTicketRequest);
    }

    private GetAuthHeaderForSystemOfflineTicketRequest getGetAuthHeaderForSystemOfflineTicketRequest(
            ConfigType configType, String userContextRealmId, String targetRealmId) {
        return new GetAuthHeaderForSystemOfflineTicketRequestBuilder()
                .setAppId(getConfigValue(configType, APP_ID))
                .setOfferingId(getConfigValue(configType, OFFERING_ID))
                .setAppSecret(getConfigValue(configType, APP_SECRET))
                .setUsername(getConfigValue(configType, USERNAME))
                .setPassword(getConfigValue(configType, PASSWORD))
                .setAssetId(getConfigValue(configType, ASSET_ID))
                .setAudiences(Collections.singletonList(getConfigValue(configType, AUDIENCES)))
                .setIp(getIpAddress())
                .setUserContextRealmId(userContextRealmId)
                .setTargetRealmId(targetRealmId)
                .setTransactionId(SpcfUniqueId.generateRandomUniqueIdString())
                .build();
    }

    public  String getOfflineTicket(ConfigType configType){
        String ticket=getOfflineTicket(configType, null, null);
        logger.info("AuthN: Identity1 Ticket received");
        return ticket;
    }

    private AuthorizationBuilder createAuthorizationBuilder(ConfigType configType){
        AuthorizationBuilder authorizationBuilder=new AuthorizationBuilder(
                getConfigValue(configType, IAMURL),
                getConfigValue(configType, APP_ID),
                getConfigValue(configType, APP_SECRET));
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
