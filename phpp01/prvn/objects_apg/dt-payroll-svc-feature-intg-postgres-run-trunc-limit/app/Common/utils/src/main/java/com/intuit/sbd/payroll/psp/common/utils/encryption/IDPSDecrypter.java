package com.intuit.sbd.payroll.psp.common.utils.encryption;

import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.IdpsRuntimeException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.IDPSManager;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Logger;

public class IDPSDecrypter {

    public static final Logger logger = Logger.getLogger(IDPSDecrypter.class.getName());
    private static IdpsClient idpsClient = IDPSManager.getIdpsClient();
    private static String keyname;

    static {
        ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration("PSP-Keys");
        keyname = config.getString("psp_idps_ptc_keyname");
    }

    public static String encryptQuickbaseData(String plainTextData) {
        Key key = idpsClient.newKeyHandleLatest(keyname);
        String encodedEncryptedText = null;
        try {
            encodedEncryptedText = Base64.getEncoder().encodeToString(key.encrypt(plainTextData.getBytes()));
        } catch (IdpsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IdpsCommunicationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return encodedEncryptedText;
    }

    public static String decryptQuickbaseData(String cipherTextData) throws IdpsCommunicationException, IdpsException {
        Key key = idpsClient.newKeyHandleLatest(keyname);
        byte[] decodedBytes = Base64.getDecoder().decode(cipherTextData);
        return new String(key.decrypt(decodedBytes));
    }

    public static void main(String[] args) throws IdpsCommunicationException, IdpsException {
        String plainText = "Well known Text";
        String cipherText = IDPSDecrypter.encryptQuickbaseData(plainText);
        logger.info("ciperText : "+cipherText);

        String s = Base64.getEncoder().encodeToString(plainText.getBytes());
        String s1 = DataEncrypter.encryptQuickbaseData(plainText);
        String decryptedText = null;
        try {
            decryptedText = IDPSDecrypter.decryptQuickbaseData(s1);
        } catch (IdpsCommunicationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IdpsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IdpsRuntimeException e){
            e.printStackTrace();
            decryptedText = DataEncrypter.decryptQuickbaseData(s1);

        }
        logger.info("decryptedText : "+decryptedText);

        String s2 = IDPSDecrypter.decryptQuickbaseData("2gEAAAABSCpQBSfgav22ZsAwZ2wJXf8DbkNSkiHvFBWI5Dg+BF9e6KNz9rE3uRR2lw==");
        logger.info("Only Decryption : "+s2);
    }

}
