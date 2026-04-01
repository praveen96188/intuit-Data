package com.intuit.sbd.payroll.psp.configuration.crypto.rsa;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 24, 2010
 * Time: 12:08:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsaCrypto {
    public static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    public static final String CIPHER_KEY_TYPE = "RSA";

    private static final KeyFactory mKeyFactory;

    static {
        try {
            mKeyFactory = KeyFactory.getInstance(CIPHER_KEY_TYPE);
        } catch (Throwable t) {
            throw new RuntimeException("Error retrieving key factory for key type " + CIPHER_KEY_TYPE + ".", t);
        }
    }

    public static byte[] crypto(byte[] data, Key key, int mode, int bufferSize) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);

            cipher.init(mode, key);

            List<ByteBuffer> outputBuffers = new Vector<ByteBuffer>();
            int bytesProcessed = 0;

            for (int offset = 0, size; offset < data.length; offset += bufferSize) {
                size = (((offset + bufferSize) < data.length) ? bufferSize : data.length - offset);

                ByteBuffer buffer = ByteBuffer.wrap(cipher.doFinal(data, offset, size));

                bytesProcessed += buffer.limit();

                outputBuffers.add(buffer);
            }

            if (bytesProcessed > 0) {
                ByteBuffer result = ByteBuffer.allocate(bytesProcessed);

                for (ByteBuffer bb : outputBuffers) {
                    result.put(bb);
                }

                return result.array();
            }

            return null;
        } catch (Throwable t) {
            throw new RuntimeException(CIPHER_TYPE + " cipher error.", t);
        }
    }

    public static byte[] encrypt(byte[] data, PublicKey key) {
        // For RSA, the maximum input data buffer size is related to the modulus of the key.
        int bufferSize = getPublicKeySpec(key).getModulus().bitLength() / 8 - 11; // encryption input buffer size
        return crypto(data, key, Cipher.ENCRYPT_MODE, bufferSize);
    }

    public static byte[] decrypt(byte[] data, PrivateKey key) {
        // For RSA, the maximum input data buffer size is related to the modulus of the key.
        int bufferSize = getPrivateKeySpec(key).getModulus().bitLength() / 8; // decryption input buffer size
        return crypto(data, key, Cipher.DECRYPT_MODE, bufferSize);
    }

    public static RSAPublicKeySpec getPublicKeySpec(PublicKey key) {
        try {
            return mKeyFactory.getKeySpec(key, RSAPublicKeySpec.class);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to retrieve public key specification.", t);
        }
    }

    public static RSAPrivateKeySpec getPrivateKeySpec(PrivateKey key) {
        try {
            return mKeyFactory.getKeySpec(key, RSAPrivateKeySpec.class);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to retrieve private key specification.", t);
        }
    }

    public static KeyPair genKeyPair(int keySize) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(CIPHER_KEY_TYPE);

            kpg.initialize(keySize);

            return kpg.genKeyPair();
        } catch (Throwable t) {
            throw new RuntimeException("Error generating key pair.", t);
        }
    }
}
