package com.intuit.sbd.payroll.psp.configuration.apps.keygen;

import com.intuit.sbd.payroll.psp.configuration.crypto.rsa.RsaCrypto;
import com.intuit.sbd.payroll.psp.configuration.crypto.rsa.RsaKeyFileWriter;
import com.intuit.sbd.payroll.psp.configuration.keys.pub.PspConfigStaticPublicKey;

import java.security.KeyPair;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 25, 2010
 * Time: 5:50:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class PspConfigKeyGenerator {
    // keep generated key pair size small (i.e. 512) so encrypted passwords are not unwieldy in length.
    private static final int KEY_SIZE = 512;
    private static final String PUB_FILE = "public.key";
    private static final String PRV_FILE = "private.key";

    public static void main(String[] args) {
        try {
            KeyPair keyPair = RsaCrypto.genKeyPair(KEY_SIZE);
            RsaKeyFileWriter.writeKeyPairToFiles(keyPair, PUB_FILE, PRV_FILE, null, PspConfigStaticPublicKey.getKey());

            System.out.println("Generated new key pair:");
            System.out.println("> Public key file:  " + PUB_FILE);
            System.out.println("> Private key file: " + PRV_FILE);
            System.out.println("Reminder: If you use these key files to manage encrypted passwords for");
            System.out.println("          the application, you will need to ensure that all passwords");
            System.out.println("          are encrypted using this new public key. Also remember to add");
            System.out.println("          these new key files to source control (rename if necessary).");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
