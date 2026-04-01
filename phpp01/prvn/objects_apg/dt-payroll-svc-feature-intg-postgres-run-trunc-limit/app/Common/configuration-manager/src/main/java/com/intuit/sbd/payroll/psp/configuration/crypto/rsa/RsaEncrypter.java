package com.intuit.sbd.payroll.psp.configuration.crypto.rsa;

import sun.misc.BASE64Encoder;

import java.security.PublicKey;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2010
 * Time: 7:24:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsaEncrypter {
    private PublicKey mPublicKey;

    public RsaEncrypter(PublicKey pPublicKey) {
        mPublicKey = pPublicKey;
    }

    public RsaEncrypter(RsaPublicKey pPublicKey) {
        this(pPublicKey.getKey());
    }

    public byte[] encrypt(byte[] data) {
        try {
            return RsaCrypto.encrypt(data, mPublicKey);
        } catch (Throwable t) {
            throw new RuntimeException("RSA encryption error.", t);
        }
    }

    public String encryptAndBase64Encode(byte[] data) {
        try {
            return new BASE64Encoder().encode(encrypt(data));
        } catch (Throwable t) {
            throw new RuntimeException("RSA encryption/encoding error.", t);
        }
    }
}
