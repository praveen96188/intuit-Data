package com.intuit.sbd.payroll.psp.common.utils.encryption;

import com.intuit.sbd.payroll.psp.common.utils.encryption.AesCrypto;
import sun.misc.BASE64Decoder;

import javax.crypto.SecretKey;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 11/6/15
 * Time: 2:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class AesDecrypter {

    private SecretKey mSecretKey;

    public AesDecrypter(SecretKey mSecretKey) {
        this.mSecretKey = mSecretKey;
    }

    public byte[] decrypt(byte[] data) {
        try {
            return AesCrypto.decrypt(data, this.mSecretKey);
        } catch (Throwable t) {
            throw new RuntimeException("AES decryption error.", t);
        }
    }

    public byte[] base64DecodeAndDecrypt(String data) {
        try {
            return decrypt(new BASE64Decoder().decodeBuffer(data));
        } catch (Throwable t) {
            throw new RuntimeException("AES decryption/decoding error.", t);
        }
    }

    public byte[] decryptData(byte[] data) {
        try {
            return AesCrypto.decryptWithIV(data, this.mSecretKey);
        } catch (Throwable t) {
            throw new RuntimeException("AES decryption error.", t);
        }
    }

    public byte[] base64DecodeAndDecryptData(String data) {
        try {
            return decryptData(new BASE64Decoder().decodeBuffer(data));
        } catch (Throwable t) {
            throw new RuntimeException("AES decryption/decoding error.", t);
        }
    }
}
