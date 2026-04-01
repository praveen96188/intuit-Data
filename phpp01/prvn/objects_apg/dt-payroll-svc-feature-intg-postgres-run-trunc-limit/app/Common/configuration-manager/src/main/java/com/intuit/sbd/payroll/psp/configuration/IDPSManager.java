package com.intuit.sbd.payroll.psp.configuration;

import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Secret;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class IDPSManager {

    public static final Logger logger = Logger.getLogger(IDPSManager.class.getName());
    private static IdpsClient idpsClient;

    public static void initialize() {
        if(idpsClient != null) {
            return;
        }
        ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration("PSP-Keys");
        String apiKeyId = config.getString("psp_idps_api_key_id");
        String apiSecretKey = config.getString("psp_idps_api_secret_key");
        String apiPolicy = config.getString("psp_idps_api_policy");
        String accessType = config.getString("psp_idps_access_type");
        String endpoint = config.getString("psp_idps_endpoint");

        Properties idpsProperties = new Properties();
        idpsProperties.setProperty("endpoint", endpoint);
        if (StringUtils.isNotEmpty(apiPolicy)) {
            logger.info("apiPolicy : " + apiPolicy);
            idpsProperties.setProperty("policy_id", apiPolicy);
            if (StringUtils.isNotBlank(accessType)) {
                idpsProperties.setProperty("access_type", accessType);
            }
        } else {
            idpsProperties.setProperty("api_key_id", apiKeyId);
            idpsProperties.setProperty("api_secret_key", apiSecretKey);
        }

        try {
            idpsClient = IdpsClient.Factory.newInstance(idpsProperties);
            idpsClient.setCryptoLocation(IdpsClient.CryptoLocation.LOCAL);
        } catch (IdpsException | IOException e) {
            logger.info("Exception : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exp) {
            logger.info("Exception : " + exp.getMessage());
            exp.printStackTrace();
        }
    }

    public static IdpsClient getIdpsClient(){
        if(idpsClient == null) {
            initialize();
        }
        return idpsClient;
    }

    public static void setIdpsClient(IdpsClient idpsClientFromIks) {
        idpsClient = idpsClientFromIks;
    }

    public static String getSecret(String secretName){
        String secretValue = null;
        try {
            if(StringUtils.isNotEmpty(secretName)){
                if (idpsClient == null) {
                    initialize();
                }
                Secret secretLatest = idpsClient.getSecretLatest(secretName);
                secretValue = secretLatest.getStringValue();
            }
        } catch (IdpsException e) {
            e.printStackTrace();
            logger.info("Error in fetching value of " + secretName + "from IDPS");
            throw new RuntimeException(e);
        } catch (IdpsCommunicationException e) {
            e.printStackTrace();
            logger.info("Error in fetching value of " + secretName + "from IDPS");
            throw new RuntimeException(e);
        }
        return secretValue;
    }

}
