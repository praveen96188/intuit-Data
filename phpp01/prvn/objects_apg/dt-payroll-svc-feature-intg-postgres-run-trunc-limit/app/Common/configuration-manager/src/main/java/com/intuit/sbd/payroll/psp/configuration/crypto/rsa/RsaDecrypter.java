package com.intuit.sbd.payroll.psp.configuration.crypto.rsa;

import sun.misc.BASE64Decoder;

import java.security.PrivateKey;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2010
 * Time: 11:45:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class RsaDecrypter {
    private PrivateKey mPrivateKey;

    public RsaDecrypter(PrivateKey pPrivateKey) {
        mPrivateKey = pPrivateKey;
    }

    public RsaDecrypter(RsaPrivateKey pPrivateKey) {
        this(pPrivateKey.getKey());
    }

    public byte[] decrypt(byte[] data) {
        try {
            return RsaCrypto.decrypt(data, mPrivateKey);
        } catch (Throwable t) {
            throw new RuntimeException("RSA decryption error.", t);
        }
    }

    public byte[] base64DecodeAndDecrypt(String data) {
        try {
            return decrypt(new BASE64Decoder().decodeBuffer(data));
        } catch (Throwable t) {
            throw new RuntimeException("RSA decryption/decoding error.", t);
        }
    }
}
