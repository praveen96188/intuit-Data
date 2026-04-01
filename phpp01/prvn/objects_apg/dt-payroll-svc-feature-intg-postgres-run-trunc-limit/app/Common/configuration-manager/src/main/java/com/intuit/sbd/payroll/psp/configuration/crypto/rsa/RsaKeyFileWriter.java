package com.intuit.sbd.payroll.psp.configuration.crypto.rsa;

import sun.misc.BASE64Encoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 24, 2010
 * Time: 12:56:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsaKeyFileWriter {
    public static void writeKeyToFile(String keyFileName, BigInteger mod, BigInteger exp, PublicKey key) throws IOException {
        FileOutputStream fos = new FileOutputStream(keyFileName);
        OutputStreamWriter osw = new OutputStreamWriter(fos);

        try {
            byte[] keyMetaData = String.format("[%s][%s]", mod.toString(), exp.toString()).getBytes();

            if (key != null) {
                keyMetaData = new RsaEncrypter(key).encrypt(keyMetaData);
            }

            osw.write(new BASE64Encoder().encode(keyMetaData));
        } catch(Throwable t) {
            throw new RuntimeException("Error writing key file " + keyFileName + ".", t);
        } finally {
            osw.flush();
            osw.close();
        }
     }

    public static void writePublicKeyToFile(String keyFileName, PublicKey keyToWrite, PublicKey encryptionKey) {
        try {
            KeyFactory factory = KeyFactory.getInstance(RsaCrypto.CIPHER_KEY_TYPE);
            RSAPublicKeySpec keySpec = factory.getKeySpec(keyToWrite, RSAPublicKeySpec.class);

            writeKeyToFile(keyFileName, keySpec.getModulus(), keySpec.getPublicExponent(), encryptionKey);
        } catch (Throwable t) {
            throw new RuntimeException("Error writing public key to file " + keyFileName + ".", t);
        }
    }

    public static void writePrivateKeyToFile(String keyFileName, PrivateKey keyToWrite, PublicKey encryptionKey) {
        try {
            KeyFactory factory = KeyFactory.getInstance(RsaCrypto.CIPHER_KEY_TYPE);
            RSAPrivateKeySpec keySpec = factory.getKeySpec(keyToWrite, RSAPrivateKeySpec.class);

            writeKeyToFile(keyFileName, keySpec.getModulus(), keySpec.getPrivateExponent(), encryptionKey);
        } catch (Throwable t) {
            throw new RuntimeException("Error writing private key to file " + keyFileName + ".", t);
        }
    }

    public static void writeKeyPairToFiles(KeyPair keyPair, String pubKeyFileName, String prvKeyFileName,
                                           PublicKey pubEncryptionKey, PublicKey prvEncryptionKey) {
        try {
            KeyFactory factory = KeyFactory.getInstance(RsaCrypto.CIPHER_KEY_TYPE);
            RSAPublicKeySpec pubKeySpec = factory.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec prvKeySpec = factory.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);

            writeKeyToFile(pubKeyFileName, pubKeySpec.getModulus(), pubKeySpec.getPublicExponent(), pubEncryptionKey);
            writeKeyToFile(prvKeyFileName, prvKeySpec.getModulus(), prvKeySpec.getPrivateExponent(), prvEncryptionKey);
        } catch (Throwable t) {
            throw new RuntimeException("Error generating key pair files.", t);
        }
    }
}
