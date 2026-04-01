package com.intuit.sbd.payroll.psp.configuration.crypto.rsa;

import sun.misc.BASE64Decoder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 24, 2010
 * Time: 12:56:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsaKeyFileReader {
    public static InputStream getKeyFileInputStream(String keyFileName) {
        // look on the classpath first...
        InputStream istream = RsaKeyFileReader.class.getResourceAsStream(keyFileName);

        // if file not found on the classpath, try explicit.
        if (istream == null) {
            try {
                istream = new FileInputStream(keyFileName);
            } catch (Throwable t) {
                throw new RuntimeException("Key file not found on classpath/filepath: " + keyFileName);
            }
        }

        return istream;
    }

    public static BigInteger[] readKeyFromFile(String keyFileName, PrivateKey key) throws IOException {
        InputStreamReader iStreamReader = new InputStreamReader(getKeyFileInputStream(keyFileName));
        BufferedReader reader = new BufferedReader(iStreamReader);

        try {
            String encodedKey = "";

            while (reader.ready()) {
                encodedKey += reader.readLine();
            }

            if (encodedKey.length() == 0) {
                throw new RuntimeException("Invalid key file " + keyFileName + " (file empty).");
            }

            byte[] keyMetaData = new BASE64Decoder().decodeBuffer(encodedKey);

            if (key != null) {
                keyMetaData = new RsaDecrypter(key).decrypt(keyMetaData);
            }

            Pattern pattern = Pattern.compile("^\\[([0-9]+)\\]\\[([0-9]+)\\]$");
            Matcher matcher = pattern.matcher(new String(keyMetaData));

            if (!matcher.matches()) {
                throw new RuntimeException("Invalid key file " + keyFileName + " (invalid key format).");
            }

            String mod = matcher.group(1);
            String exp = matcher.group(2);

            return new BigInteger[] {new BigInteger(mod), new BigInteger(exp)};
        } finally {
            reader.close();
        }
    }

    public static PublicKey readPublicKeyFromFile(String keyFileName, PrivateKey decryptionKey) {
        try {
            BigInteger[] pubKeyMeta = readKeyFromFile(keyFileName, decryptionKey);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(pubKeyMeta[0], pubKeyMeta[1]);
            KeyFactory factory = KeyFactory.getInstance(RsaCrypto.CIPHER_KEY_TYPE);

            return factory.generatePublic(keySpec);
        } catch (Throwable t) {
            throw new RuntimeException("Error reading public key file " + keyFileName + ".", t);
        }
    }

    public static PrivateKey readPrivateKeyFromFile(String keyFileName, PrivateKey decryptionKey) {
        try {
            BigInteger[] pubKeyMeta = readKeyFromFile(keyFileName, decryptionKey);
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(pubKeyMeta[0], pubKeyMeta[1]);
            KeyFactory factory = KeyFactory.getInstance(RsaCrypto.CIPHER_KEY_TYPE);

            return factory.generatePrivate(keySpec);
        } catch (Throwable t) {
            throw new RuntimeException("Error reading private key file " + keyFileName + ".", t);
        }
    }
}
