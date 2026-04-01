package com.intuit.sbd.payroll.psp.common.utils.encryption;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.configuration.keys.prv.PspConfigStaticPrivateKey;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 11/6/15
 * Time: 1:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataEncrypter {
    public static final Logger logger = Logger.getLogger(DataEncrypter.class.getName());
    private static SecretKey pspQbappSecretKey;
    private static PrivateKey pspPrivatekey;


    static {
        String keyFile = ConfigurationManager.getSettingValue("PSP-Keys", "psp_atp_qbapp_key_file");
        pspPrivatekey = PspConfigStaticPrivateKey.getKey();
        pspQbappSecretKey = RsaAesKeyFileReader.readSecretKeyFromFile(keyFile, "psp_atp_qbapp_key_file", pspPrivatekey);
    }

    public static String encryptQuickbaseData(String plainTextData) {
        return new AesEncrypter(pspQbappSecretKey).encryptAndBase64EncodeData(plainTextData.getBytes());
    }

    public static String decryptQuickbaseData(String cipherTextData) {
        return new String(new AesDecrypter(pspQbappSecretKey).base64DecodeAndDecryptData(cipherTextData));
    }
}
