package com.intuit.sbd.payroll.psp.configuration.apps.password;

import com.intuit.sbd.payroll.psp.configuration.crypto.rsa.RsaEncrypter;
import com.intuit.sbd.payroll.psp.configuration.crypto.rsa.RsaPublicKey;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 25, 2010
 * Time: 5:50:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class PspConfigPasswordEncrypter {
    private static void usage() {
        System.out.println("====================================================================");
        System.out.println("Usage: PspConfigPasswordEncrypter <keyfile> <password>");
        System.out.println("Where: <keyfile> is the public key file used to encrypt the password");
        System.out.println("       <password> is the password to be encrypted");
        System.out.println("====================================================================");
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                throw new RuntimeException("Invalid command line.");
            }

            RsaEncrypter enc = new RsaEncrypter(new RsaPublicKey(args[0]));
            String encPassword = enc.encryptAndBase64Encode(args[1].getBytes());

            encPassword = "ENC(" + encPassword.replaceAll("\r?\n", "") + ")";

            System.out.println("Encrypted password: " + encPassword);
        } catch (Throwable t) {
            t.printStackTrace();
            usage();
        }
    }
}
