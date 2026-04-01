package com.intuit.sbd.payroll.psp.common.utils.encryption;

import org.apache.commons.lang.ArrayUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 11/6/15
 * Time: 1:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class AesCrypto {

    private static Logger logger = Logger.getLogger(AesCrypto.class.getName());

    public static byte[] crypto(byte[] data, SecretKey key, int mode) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, key);
            byte[] cipherText = cipher.doFinal(data);
            return cipherText;
        } catch (NoSuchAlgorithmException t) {
            throw new RuntimeException("Cipher error.", t);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("Cipher error.", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Cipher error.", e);
        } catch (BadPaddingException e) {
            throw new RuntimeException("Cipher error.", e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("Cipher error.", e);
        }
    }

    public static byte[] encrypt(byte[] data, SecretKey key) {
        return crypto(data, key, Cipher.ENCRYPT_MODE);
    }

    public static byte[] decrypt(byte[] data, SecretKey key) {
        return crypto(data, key, Cipher.DECRYPT_MODE);
    }

    public static byte[] encryptWithIV(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getEncoded(), "AES"));
            byte[] iv = cipher.getIV();
            byte[] cipherText = cipher.doFinal(data);
            byte[] finalCipherText = ArrayUtils.addAll(iv, cipherText);
            return finalCipherText;
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static byte[] decryptWithIV(byte[] data, SecretKey key) {
        try {
            byte[] actualCipherText = Arrays.copyOfRange(data, 16, data.length);
            byte[] receivedIv = ArrayUtils.subarray(data, 0, 16);
            Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            decryptCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getEncoded(), "AES"), new IvParameterSpec(receivedIv));
            byte[] deCiphered = decryptCipher.doFinal(actualCipherText);
            return deCiphered;
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static SecretKey genKey(int keySize) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(keySize);
            return kg.generateKey();
        } catch (Throwable t) {
            throw new RuntimeException("Error generating key pair.", t);
        }
    }
}
