package com.intuit.sbd.payroll.psp.adapters.sap.adapter.utils;

import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.IdpsRuntimeException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbd.payroll.psp.common.utils.encryption.DataEncrypter;
import com.intuit.sbd.payroll.psp.common.utils.encryption.IDPSDecrypter;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Base64;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 11/6/15
 * Time: 2:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataEncrypterTest {

    Logger logger = Logger.getLogger(DataEncrypterTest.class.getName());
    /*
    * This test is to test whether the keys are getting picked properly or not
    */
    @Ignore
    @Test
    public void testEncryption() {
        String plainText = "This is our plain text";
        String cipherText = DataEncrypter.encryptQuickbaseData(plainText);
        logger.info("cipherText : "+cipherText);
        String decryptedText = DataEncrypter.decryptQuickbaseData(cipherText);
        logger.info("decryptedText : "+decryptedText);
    }

    @Test
    public void testIdpsEncryption() {

        String plainText = "Well known Text";
        String cipherText = IDPSDecrypter.encryptQuickbaseData(plainText);
        logger.info("ciperText : "+cipherText);

        String s = Base64.getEncoder().encodeToString(plainText.getBytes());
        String s1 = DataEncrypter.encryptQuickbaseData(plainText);
        String decryptedText = null;
        try {
            decryptedText = IDPSDecrypter.decryptQuickbaseData(s1);

            logger.info("decryptedText : "+decryptedText);

            String s2 = IDPSDecrypter.decryptQuickbaseData("2gEAAAABSCpQBSfgav22ZsAwZ2wJXf8DbkNSkiHvFBWI5Dg+BF9e6KNz9rE3uRR2lw==");
            logger.info("Only Decryption : "+s2);
        } catch (IdpsCommunicationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IdpsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IdpsRuntimeException e){
            e.printStackTrace();
            decryptedText = DataEncrypter.decryptQuickbaseData(s1);
            logger.info("decryptedText : "+decryptedText);
        }
    }
}
