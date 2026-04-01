package com.intuit.sbd.payroll.psp.common.utils.encryption;

import com.intuit.sbd.payroll.psp.configuration.crypto.rsa.RsaDecrypter;
import sun.misc.BASE64Decoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.PrivateKey;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 11/6/15
 * Time: 1:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsaAesKeyFileReader {

    public static BigInteger[] readKeyFromFile(String keyFile, PrivateKey key)
            throws IOException {
        byte[] keyMetaData = new BASE64Decoder().decodeBuffer(keyFile);
        if (key != null) {
            keyMetaData = new RsaDecrypter(key).decrypt(keyMetaData);
        }
        return new BigInteger[]{new BigInteger(keyMetaData)};
    }

    public static SecretKey readSecretKeyFromFile(String keyFile, String keyName, PrivateKey decryptionKey) {
        try {
            BigInteger[] secretKeyMeta = readKeyFromFile(keyFile, decryptionKey);
            SecretKeySpec keySpec = new SecretKeySpec(secretKeyMeta[0].toByteArray(), "AES");
            SecretKey secretKey = new SecretKeySpec(secretKeyMeta[0].toByteArray(), 0, secretKeyMeta[0].toByteArray().length, "AES");
            return secretKey;
        } catch (Throwable t) {
            throw new RuntimeException("Error reading public key secret" + keyName, t);
        }
    }

}
