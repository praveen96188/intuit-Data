package com.intuit.sbd.payroll.psp.configuration.crypto.rsa;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 24, 2010
 * Time: 8:13:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsaPublicKey {
    private PublicKey mKey = null;

    public RsaPublicKey(PublicKey key) {
        mKey = key;
    }

    public RsaPublicKey(String keyFile) {
        this(keyFile, null);
    }

    public RsaPublicKey(String keyFile, PrivateKey decryptionKey) {
        this(RsaKeyFileReader.readPublicKeyFromFile(keyFile, decryptionKey));
    }

    public PublicKey getKey() {
        return mKey;
    }
}
