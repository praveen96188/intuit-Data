package com.intuit.sbd.payroll.psp.configuration.keys.prv;

import sun.misc.BASE64Decoder;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2010
 * Time: 7:16:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class PspConfigStaticPrivateKey {
    /* Base64 encoded 2048-bit private key meta-data ( format is: [key-modulus][key-exponent] ) */
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
                                      "NzIzNjE5OTE5ODcxOTI4NTM0ODQ4OTE4NDU3MzUyMTAxMzA5MjE1NzkxNTg2OTAzXVsxOTgzMjkx" +
                                      "NTAxMTEzMDQ5OTM2MDA1MTMwMjI2NTI1ODk4NTA2OTY5NzIzMDM1Mjk1ODA2MzY0OTQ3NDIzMjg5" +
                                      "Mjc3OTk0MTQ0NTIzMzcyOTcwNTgwMDY4NDI3Mzc2MjkyMDQ0ODY0MDAwODAzNzEzODQ0MTcxNTAx" +
                                      "NzU4OTA0MDkzNTc4OTU5ODQ0NjY3MTA5MTE2MTY3OTcwMTE5MzMzNTI0NDA2MzA2NzA0MDgzNjky" +
                                      "NDg4NTIwNjI3MjA5NTU0NTg1NTk3NTA2NjA3MjE1NTg5MzUwMTkwMTI4NTg5ODYxNTI2NTkxOTcw" +
                                      "MjY3MTg5Mjg3Mjk0MjY3MjQyMzE3NjEwMTkwMzU5NTE2NDQ2ODY2NDQ2NjgwMzc5NDc3NzgxNzAx" +
                                      "NzI2NTY3NzExNjY0MzA4NjEzMTk0OTM5MzkyOTg5MDg1ODE3ODk0NDgxNTA2MzczOTY0MTMyMTkz" +
                                      "MzExMzE2Mjc1NjU1Nzk1MjY1NDM2OTQ4MzgzOTI1MjQwNjc1MjYxNTM3MTUxNTcwNzUwNzE2NTM3" +
                                      "NTIwODk1NjQxMzc4MTE0ODA4MzQ0NjExNTY3MDAwNDMwNjIxOTMwOTM2MDIzMzY4NjQ0OTU3Mzkw" +
                                      "MDc0NzA2NjM1MDM5Nzg1NDI5Nzk5ODI4MTQ3MDQzNjU5ODk3MjM4NDcxNzUzNTQ5OTc0ODEwMDU3" +
                                      "MTY4NzIyNDEwMDI0NDQ0MjUzNzM1NjY3NzYyMzk3MzA5ODgzNDg1OTczMDU5NzYwODgwMDIxMzEy" +
                                      "NDg1ODIwMDAwNjk5NjIxMTgyNDQ1MjIzMjczMTY4Nzc3NTkzNTc5M10=";
    private static PrivateKey key = null;

    private PspConfigStaticPrivateKey() {
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

            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(mod), new BigInteger(exp));
            KeyFactory factory = KeyFactory.getInstance("RSA");

            key = factory.generatePrivate(keySpec);
        } catch (Throwable t) {
            throw new RuntimeException("Key initialization failed.", t);
        }
    }

    public static PrivateKey getKey() {
        if (key == null) {
            initializeKey();
        }

        return key;
    }
}
