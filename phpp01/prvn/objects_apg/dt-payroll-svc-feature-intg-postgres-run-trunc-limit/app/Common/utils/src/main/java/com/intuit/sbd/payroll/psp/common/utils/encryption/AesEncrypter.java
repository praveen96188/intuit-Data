package com.intuit.sbd.payroll.psp.common.utils.encryption;

import sun.misc.BASE64Encoder;

import javax.crypto.SecretKey;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 11/6/15
 * Time: 1:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class AesEncrypter {

    private SecretKey secretKey;

    public AesEncrypter(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     *
     * @param data
     * @return
     */
    public byte[] encrypt(byte[] data) {
        try {
            return AesCrypto.encrypt(data, this.secretKey);
        } catch (Throwable t) {
            throw new RuntimeException("RSA encryption error.", t);
        }
    }

    public String encryptAndBase64Encode(byte[] data) {
        try {
            return new BASE64Encoder().encode(encrypt(data));
        } catch (Throwable t) {
            throw new RuntimeException("AES encryption/encoding error.", t);
        }
    }

    public byte[] encryptData(byte[] data) {
        try {
            return AesCrypto.encryptWithIV(data, this.secretKey);
        } catch (Throwable t) {
            throw new RuntimeException("RSA encryption error.", t);
        }
    }

    public String encryptAndBase64EncodeData(byte[] data) {
        return new BASE64Encoder().encode(encryptData(data));
    }
}
