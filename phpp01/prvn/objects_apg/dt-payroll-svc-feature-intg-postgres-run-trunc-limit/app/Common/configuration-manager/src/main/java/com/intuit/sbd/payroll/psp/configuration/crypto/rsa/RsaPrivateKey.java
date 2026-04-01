package com.intuit.sbd.payroll.psp.configuration.crypto.rsa;

import java.security.PrivateKey;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 24, 2010
 * Time: 8:01:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsaPrivateKey {
    private PrivateKey mKey = null;

    public RsaPrivateKey(PrivateKey key) {
        mKey = key;
    }

    public RsaPrivateKey(String keyFile) {
        this(keyFile, null);
    }

    public RsaPrivateKey(String keyFile, PrivateKey decryptionKey) {
        this(RsaKeyFileReader.readPrivateKeyFromFile(keyFile, decryptionKey));
    }

    public PrivateKey getKey() {
        return mKey;
    }
}
