package com.intuit.ems.payroll.psp.jss;

import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;
import java.util.Base64;
import java.nio.charset.Charset;

public class TestEncoding {

    public static void main(String[] args) throws IdpsCommunicationException, IdpsException, Exception {
        String plainText;
        String cipherText = "2gIAAAASAQAEAAAAAgIACHlCd0ZzcWhEOJe0PBHNc+HQj/hY/MXhTfHmaXvaTttwqla3GDqBZHbvRZCR4w==";
        TestEncoding testEncoding = new TestEncoding();
        IdpsClient client = testEncoding.initialize();

        String keyName = "PSP/QBDT_PItmInfo_AID_AES256SIV";
        Key key = client.newKeyHandleLatest(keyName);
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        byte[] decryptedBytes = new byte[0];

        decryptedBytes = key.deriveTwiceAndDecryptLocal(decodedBytes);

        plainText = new String(decryptedBytes);
        String plainTextUTF8 = new String(decryptedBytes, Charset.forName("UTF-8"));
        System.out.println("CipherText: " + cipherText);
        System.out.println("PlainText: " + plainText);
        System.out.println("PlainTextUTF8: " + plainTextUTF8);

    }

    public IdpsClient initialize() throws IdpsException, Exception {
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


        IdpsClient idpsClient = IdpsClient.Factory.newInstance(idpsProperties);
        idpsClient.setCryptoLocation(IdpsClient.CryptoLocation.LOCAL);
        System.out.println("successfully connected with IDPS ");
        return idpsClient;

    }
}
