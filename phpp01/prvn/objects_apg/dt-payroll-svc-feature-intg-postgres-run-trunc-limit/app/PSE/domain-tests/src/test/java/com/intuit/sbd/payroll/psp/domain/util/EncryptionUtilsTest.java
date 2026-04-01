package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbd.payroll.psp.cache.spring.IDPSCacheService;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 4/26/18
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class EncryptionUtilsTest {
    public static SpcfLogger logger = SpcfLogManager.getLogger(EncryptionUtilsTest.class);

    String plainText="Payroll Services Platform@#$%&^*!";
    String deterministicKeyName = "Company_FedTaxId";
    String probabilisticKeyName = "Company_PrivateKey";
    String derivationSeed = "some text";

    @Test
    public void testDeterministicEncryption() throws Exception {
        String cipherText = EncryptionUtils.deterministicEncrypt(deterministicKeyName, plainText);
        String decryptedText = EncryptionUtils.deterministicDecrypt(deterministicKeyName, cipherText);
        Assert.assertEquals("Decrypted value not matching plaintext value", plainText, decryptedText);
    }

    @Test
    public void testProbabilisticEncrypt() throws Exception {
        String cipherText = EncryptionUtils.probabilisticEncrypt(probabilisticKeyName, plainText, derivationSeed);
        String decryptedText = EncryptionUtils.probabilisticDecrypt(probabilisticKeyName, cipherText);
        Assert.assertEquals("Decrypted value not matching plaintext value", plainText, decryptedText);
    }
    @Test
    public void testDateFormatUtil() throws Exception {

        String derivationSeed = "some text";
        SpcfCalendar instance = SpcfCalendar.createInstance();

        String cipherText = EncryptionUtils.probabilisticEncryptDate(probabilisticKeyName, instance, derivationSeed);
        SpcfCalendar decryptedDate = EncryptionUtils.probabilisticDecryptDate(probabilisticKeyName, cipherText);
        Assert.assertEquals("Decrypted value not matching plaintext value", instance, decryptedDate);
    }

    @Test
    public void testEncryptionWithAllKeys(){
        List<String> cipherText = EncryptionUtils.deterministicEncryptWithAllKeys(deterministicKeyName, plainText);
        String encryptedTextWithLatestKey = EncryptionUtils.deterministicEncrypt(deterministicKeyName, plainText);
        Assert.assertTrue("Encrypted value does not exist in the list of encrypted values", cipherText.contains(encryptedTextWithLatestKey));
        List<String> stringList = EncryptionUtils.deterministicEncryptWithAllKeys(deterministicKeyName, null);
        Assert.assertEquals("Encrypting a null value returns an incorrect number of values", 1, stringList.size());
        Assert.assertEquals("Encrypting a null value returns an incorrect number of values", "NONE", stringList.get(0));
    }

    @Test
    public void testRefreshKeys() {
        IDPSCacheService idpsCacheService = PayrollApplicationBeanFactory.getBean(IDPSCacheService.class);
        String keyName = "PSP/Company_FedTaxId_AES256SIV";
        try {
            //Fetch the keys and make note of the size
            List<Key> keys = idpsCacheService.getKeys(keyName);
            int initialKeysSize = keys.size();

            // Evict the latest key version and fetch the keys again
            Key key = keys.get(keys.size() - 1);
            idpsCacheService.evictVersion(keyName, key.getVersion());
            List<Key> latestkeys = idpsCacheService.getKeys(keyName);
            Assert.assertNotSame("Checking the number of keys", initialKeysSize, latestkeys.size());
            Assert.assertEquals("Checking the number of keys to be 1 less", initialKeysSize - 1, latestkeys.size());

            // Evict the key name and fetch the keys again
            idpsCacheService.evict(keyName);
            List<Key> latestkeys1 = idpsCacheService.getKeys(keyName);
            Assert.assertEquals("Checking the number of keys", initialKeysSize, latestkeys1.size());
        } catch (IdpsCommunicationException e) {
            e.printStackTrace();
        } catch (IdpsException e) {
            e.printStackTrace();
        }
    }


}