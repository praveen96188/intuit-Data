package com.intuit.sbd.payroll.psp.common.utils.encryption.idps;

import com.intuit.idps.IdpsClient;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.domain.item.Key;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * IDPS Configuration manager
 */
public class IDPSFileStreamManager {

    public static final Logger logger = Logger.getLogger(IDPSFileStreamManager.class.getName());

    private static IdpsClient idpsClient ;

    private static String keyName ;

    public static IdpsClient getIdpsClient() {
        return idpsClient;
    }

    static {
        ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration("PSP-Keys");
        String apiKeyId = config.getString("psp_idps_api_key_id");
        String apiSecretKey = config.getString("psp_idps_api_secret_key");
        String apiPolicy = config.getString("psp_idps_api_policy");
        String accessType = config.getString("psp_idps_access_type");
        String endpoint = config.getString("psp_idps_endpoint");
        keyName = config.getString("psp_idps_batchjobs_keyname");
        Properties idpsProperties = new Properties();
        idpsProperties.setProperty("endpoint", endpoint);
        if(!apiPolicy.isEmpty()){
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
            logger.info("Exception : "+e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception exp){
            logger.info("Exception : "+exp.getMessage());
            throw new RuntimeException(exp);
        }
    }

    /**
     * Get IDPS key handle
     * @return
     */
    public static Key newKeyHandleLatest()
    {
        Key key = null;
        try {
             key = idpsClient.newKeyHandleLatest(keyName);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            logger.info("Error in fetching key  " + keyName + "from IDPS");
            throw new RuntimeException(ex);
        }
        return key;
    }
   /* public static void main(String[] args)
    {
        Key key = null;
        try {
            boolean allowOverwrite = false;
           // idpsClient.generateKey(keyName, Key.KeyAlgorithm.AES256_GCM, allowOverwrite);
            key = newKeyHandleLatest();
        }
        catch(Exception ex)
        {
            logger.info("Exception : "+ ex.getMessage());
        }
    }*/
}
