package com.intuit.sbd.payroll.psp.configuration.keys.pub;

import sun.misc.BASE64Decoder;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2010
 * Time: 6:47:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class PspConfigStaticPublicKey {
    /* Base64 encoded 2048-bit public key meta-data ( format is: [key-modulus][key-exponent] ) */
    private static final String KEY = "WzI4NTYzNjY4ODUxNDMzMDE5MTUzMDUzMTE5MzYxNzkwNTMwODEwMDgxMjUyMjk0MDczNDU2MDQ2" +
                                      "NDkxNDM2MTI5OTY2MzU1ODM5OTM1ODk1OTE3NTcwODA0MjAyODI2MDc0NDQxMTA1ODE3MDkxMDg3" +
                                      "NjE3OTQ2OTc1MjI3NjIyODA2NDc5MzUyMDE0MDUzMDMzNzY0NzA3MjMxNjY5MjE0NjMzMzQwNTM4" +
                                      "OTQ0MTg5MjAwMDA0NDMzODc0Mzk0NTg5MzU4NTk4Nzg5MjU3NjgzMzQ4OTE2MjUzMjU5NTA4NTcx" +
                                      "NTU2NTg4OTM2NDY4NjkyMzAyMjUzNDY0OTUxMDUzNTU4NzM5ODI4OTQ4MzMwNTc2MDUwMTI0NjA4" +
                                      "MjQ4NjI3NDg4NDE5NzQ5MDY0NzkzNzEzOTA0MjkxMzk4NDA5NDk2MjA5ODI1NDA5ODgzNTg1MjE1" +
                                      "NDM3OTExMTM0MjQyMjEzNTA3Njc3OTQ2NTg1NDcxOTk3MTQ3OTIyNzg4MDk5Mjk2NzM4NjY2Mzkw" +
                                      "OTY3MTQyMzA5NTI2NTAwOTQ2Mzg2MDkwMjYxNTg3OTEzNTE3MzA4MjkxMDY3Mjk4MDI4OTQ1MTU2" +
                                      "MzI1OTk3NTIzMDYyODk5MzI5MTM5MjI3NzExOTA3NjI3MjI0NDQyMjA4NDk5MTk4MTI5NTkzOTI4" +
                                      "Nzg4NjgyMzcyNjkzNTM1ODM5NTkwNTkyNjY0ODk5MDQ1NDcyMDk2ODM3OTQ4MDg5OTMyMzM4MjI4" +
                                      "NzIzNjE5OTE5ODcxOTI4NTM0ODQ4OTE4NDU3MzUyMTAxMzA5MjE1NzkxNTg2OTAzXVs2NTUzN10=";
    private static PublicKey key = null;

    private PspConfigStaticPublicKey() {
    }

    synchronized private static void initializeKey() {
        if (key != null) return; // in case multiple threads blocked during initialization.

        try {
            byte[] keyMetaData = new BASE64Decoder().decodeBuffer(KEY);
            Pattern pattern = Pattern.compile("^\\[([0-9]+)\\]\\[([0-9]+)\\]$");
            Matcher matcher = pattern.matcher(new String(keyMetaData));

            if (!matcher.matches()) {
                throw new RuntimeException("Invalid key format.");
            }

            String mod = matcher.group(1); // key modulus
            String exp = matcher.group(2); // key exponent

            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(mod), new BigInteger(exp));
            KeyFactory factory = KeyFactory.getInstance("RSA");

            key = factory.generatePublic(keySpec);
        } catch (Throwable t) {
            throw new RuntimeException("Key initialization failed.", t);
        }
    }

    public static PublicKey getKey() {
        if (key == null) {
            initializeKey();
        }

        return key;
    }
}
