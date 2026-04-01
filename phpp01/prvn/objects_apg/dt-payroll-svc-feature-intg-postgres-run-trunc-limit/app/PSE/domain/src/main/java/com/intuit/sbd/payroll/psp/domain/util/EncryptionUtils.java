package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.domain.item.ListedVersion;
import com.intuit.idps.domain.item.VersionIterator;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbd.payroll.psp.cache.spring.IDPSCacheService;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EncryptionUtils {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(EncryptionUtils.class);
    private static IdpsClient idpsClient;
    private static IdpsClient idpsClientForBRM;
    private static final String BRM_IDPS_ENDPOINT = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_idps_endpoint");
    private static final String BRM_IDPS_POLICY = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_idps_apipolicy");
    private static final String BRM_IDPS_ACCESSTYPE = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_idps_accesstype");
    private static final String KEY_PREFIX = "PSP/";
    private static final String DETERMINISTIC_KEY_SUFFIX = "_AES256SIV";
    private static final String PROBABILISTIC_KEY_SUFFIX = "_AES256GCM";
    private static FeatureFlagLazyLoader featureFlagLazyLoader = FeatureFlagLazyLoader.getInstance();
    private static IDPSCacheService idpsCacheService;


    public static String deterministicEncrypt(String keyName, String plainText) {

        if (StringUtils.isEmpty(plainText)) {
            return null;
        }
        String fullKeyName = getDeterministicKeyName(keyName);

        try {
            Key key = getIdpsClient().newKeyHandleLatest(fullKeyName);
            byte[] encryptedBytes = key.deriveTwiceAndEncryptLocalDeterministic(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Exception while encrypting. ", e);
            throw new IdpsEncryptionException(e, "Issue with IDPS service.");
        }
    }

    @NotNull
    private static String getDeterministicKeyName(String keyName) {
        return KEY_PREFIX + keyName + DETERMINISTIC_KEY_SUFFIX;
    }

    public static String deterministicDecrypt(String keyName, String cipherText) {
        String fullKeyName = getDeterministicKeyName(keyName);
        return decryptCipherText(fullKeyName, cipherText);
    }

    public static String probabilisticEncrypt(String keyName, String plainText, String derivationSeed) {
        if (StringUtils.isEmpty(plainText)) {
            return null;
        }

        String fullKeyName = getProbabilisticKeyName(keyName);
        try {
            Key key = getIdpsClient().newKeyHandleLatest(fullKeyName);

            byte[] encryptedBytes = key.deriveTwiceAndEncryptLocalProbabilistic(derivationSeed, plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Exception while encrypting. ", e);
            throw new IdpsEncryptionException(e, "Issue with IDPS service.");
        }
    }

    @NotNull
    private static String getProbabilisticKeyName(String keyName) {
        return KEY_PREFIX + keyName + PROBABILISTIC_KEY_SUFFIX;
    }

    public static String probabilisticDecrypt(String keyName, String cipherText) {
        String fullKeyName = getProbabilisticKeyName(keyName);
        return decryptCipherText(fullKeyName, cipherText);
    }

    public static String probabilisticEncryptDate(String keyName, SpcfCalendar plainTextDate, String derivationSeed) {
        if (Objects.isNull(plainTextDate)) {
            return null;
        }

        String fullKeyname = getProbabilisticKeyName(keyName);
        try {
            Key key = getIdpsClient().newKeyHandleLatest(fullKeyname);
            long timeInMilliseconds = plainTextDate.getTimeInMilliseconds();
            String plainText = String.valueOf(timeInMilliseconds);
            byte[] encryptedBytes = key.deriveTwiceAndEncryptLocalProbabilistic(derivationSeed, plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Exception while encrypting. ", e);
            throw new IdpsEncryptionException(e, "Issue with IDPS service.");
        }
    }

    public static SpcfCalendar probabilisticDecryptDate(String keyName, String cipherText) {
        String fullKeyname = getProbabilisticKeyName(keyName);
        SpcfCalendar decryptedTextDate = null;

        String decryptedText = decryptCipherText(fullKeyname, cipherText);
        if (decryptedText != null) {
            long timeInMilliseconds = Long.valueOf(decryptedText);
            decryptedTextDate = SpcfCalendar.createInstance(timeInMilliseconds);
        }
        return decryptedTextDate;
    }

    public static String decryptCipherText(String keyName, String cipherText) {
        if (StringUtils.isEmpty(cipherText)) {
            return cipherText;
        }

        try {
            Key key = getIdpsClient().newKeyHandleLatest(keyName);
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = key.deriveTwiceAndDecryptLocal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IdpsException | IdpsCommunicationException e) {
            logger.error("Error while decrypting cipherText=" + cipherText + " with key=" + keyName, e);
            throw new IdpsEncryptionException(e, "Issue with IDPS service.");
        }
    }

    public static List<String> deterministicEncryptWithAllKeys(String keyName, String plainText) {
        List<String> cipherTextList = new ArrayList<>();
        if (StringUtils.isEmpty(plainText)) {
            cipherTextList.add("NONE");
            return cipherTextList;
        }
        try {
            String fullKeyName = getDeterministicKeyName(keyName);
            List<Key> keyList = getIdpsCacheService().getKeys(fullKeyName);
            for (Key key : keyList) {
                byte[] encryptedBytes = key.deriveTwiceAndEncryptLocalDeterministic(plainText.getBytes(StandardCharsets.UTF_8));
                String cipherText = Base64.getEncoder().encodeToString(encryptedBytes);
                cipherTextList.add(cipherText);
            }
        } catch (Exception e) {
            logger.error("Exception while encrypting. ", e);
            throw new IdpsEncryptionException(e, "Issue with IDPS service.");
        }
        return cipherTextList;
    }

    public static void initialize() {

        synchronized (EncryptionUtils.class) {
            ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration("PSP-Keys");
            String apiKeyId = config.getString("psp_idps_api_key_id");
            String apiSecretKey = config.getString("psp_idps_api_secret_key");
            String endpoint = config.getString("psp_idps_endpoint");
            String apiPolicy = config.getString("psp_idps_api_policy");
            String accessType = config.getString("psp_idps_access_type");
            String keyCacheMaxElements = config.getString("psp_idps_key_cache_max_size");

            Properties idpsProperties = new Properties();
            idpsProperties.setProperty("endpoint", endpoint);
            idpsProperties.setProperty("key_cache_max_elements", keyCacheMaxElements);
            if (!apiPolicy.isEmpty()) {
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
                logger.info("successfully connected with IDPS ");
            } catch (IOException | IdpsException e) {
                logger.error("Unable to create IDPS client Object to connect with IDPS ", e);
                throw new IdpsEncryptionException(e, "Issue with IDPS service.");
            }
        }
    }

    public static Key getIDPSKeyForBRMEncryption(String keyName) {

        Key key;
        try {
            if (idpsClientForBRM == null) {
                initializeForBRM();
            }
            idpsClientForBRM.setCryptoLocation(IdpsClient.CryptoLocation.LOCAL);
            key = idpsClientForBRM.newKeyHandleLatest(keyName);
        } catch (Exception e) {
            logger.error("Exception while getting IDPS Key for encrypting usage file for BRM. ", e);
            throw new IdpsEncryptionException(e, "Issue with IDPS service.");
        }

        return key;
    }

    public static void initializeForBRM() {
        if (idpsClientForBRM == null) {
            synchronized (EncryptionUtils.class) {

                Properties idpsProperties = new Properties();

                idpsProperties.setProperty("endpoint", BRM_IDPS_ENDPOINT);

                if (!BRM_IDPS_POLICY.isEmpty()) {
                    idpsProperties.setProperty("policy_id", BRM_IDPS_POLICY);
                    if (StringUtils.isNotBlank(BRM_IDPS_ACCESSTYPE)) {
                        idpsProperties.setProperty("access_type", BRM_IDPS_ACCESSTYPE);
                    }
                } else {
                    logger.info("Policy Id is empty for BRM");
                }

                try {
                    idpsClientForBRM = IdpsClient.Factory.newInstance(idpsProperties);
                    idpsClientForBRM.setCryptoLocation(IdpsClient.CryptoLocation.LOCAL);
                    logger.info("BRM Idps Client is healthy -" + idpsClientForBRM.isHealthy());
                    logger.info("successfully connected with BRM IDPS ");
                } catch (IOException | IdpsException e) {
                    logger.error("Unable to create IDPS client Object to connect with BRM IDPS ", e);
                    throw new IdpsEncryptionException(e, "Issue with IDPS service.");
                }
            }
        }
    }


    public static IdpsClient getIdpsClient() {
        if(Objects.isNull(idpsClient)){
            initialize();
        }
        return idpsClient;
    }
    public static void setIdpsClient(IdpsClient idpsClientFromIks) {
        idpsClient = idpsClientFromIks;
    }

    public static IDPSCacheService getIdpsCacheService(){
        if(Objects.isNull(idpsCacheService)){
            idpsCacheService = PayrollApplicationBeanFactory.getBean(IDPSCacheService.class);
        }
        return idpsCacheService;
    }
}

class IdpsEncryptionException extends RuntimeException {
    public IdpsEncryptionException(Throwable throwable, String message) {
        super(message, throwable);
    }
}