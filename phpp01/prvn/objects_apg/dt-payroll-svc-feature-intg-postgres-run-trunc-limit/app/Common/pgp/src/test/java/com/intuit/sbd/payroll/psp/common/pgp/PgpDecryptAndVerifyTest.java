package com.intuit.sbd.payroll.psp.common.pgp;

import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileDecryptionResult;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * Created with IntelliJ IDEA.
 * User: kpaul
 * Date: 12/6/12
 * Time: 11:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class PgpDecryptAndVerifyTest {

    //
    // Note: For the configured JDK in the project, the following must be set correctly or you will see an "invalid key size" error:
    //
    // 1) The Bouncy Castle jars (bcpg-jdk15-147 & bcprov-ext-jdk15-147) must be copied into the jre/lib/ext directory for your JRE
    //
    // 2) The two 'unlimited strength' policy jars (local_policy & US_export_policy) must be copied into the jre/lib/security directory for your JRE
    //    (see "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" on the Oracle web site)
    //

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        //
        // This will test the signature validation and decryption of a PGP encrypted+signed message (aka file)
        //
        // - We need to validate the signature of the file using Intuit's public key (Intuit signed the file using their private key).
        // - We need to decrypt the file using the bank's private key (Intuit encrypted the file using the bank's public key).
        //
        // > This will ensure our signature validation and decryption code are working properly.
        //

        String workingFolder = PgpEncryptAndSignTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String achFileName = "d.1354929319592CCD";
        String txtFileName = achFileName + ".txt";
        String pgpFileName = achFileName + ".pgp";

        //String decKeyFileName = "C:/Dev/PSP/dev/Common/PGP/resources/bank-test-prv-20141206.asc"; // Test private key (bank owned) used to decrypt the file
        //String keyPass = "jpmc-ach"; // Private key password

        String decKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key"); // Test private key (bank owned) used to decrypt the file
        String keyPass = "intuit-ach"; // Private key password

        String sigKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key"); // Test public key (intuit owned) used to verify the signature


        PgpFileUtils.deleteFile(workingFolder + txtFileName);

        StopWatch sw = StopWatch.create(false);

        System.out.println("Starting pgpDecryptAndVerifyFile...");

        sw.start();

        PgpFileDecryptionResult result = PgpFileUtils.pgpDecryptAndVerifyFile(workingFolder,
                                                                              pgpFileName,    // The encrypted+signed file to be decrypted and validated
                                                                              txtFileName,    // The preferred 'default' file name for the decrypted file
                                                                              sigKey, // The Intuit Public Key file (used to verify the signature of the signed file)
                                                                              decKey, // The JPMC Private Key file (used to decrypt the encrypted file)
                                                                              keyPass);       // The password for the private key

        sw.stop();

        System.out.println("Completed pgpDecryptAndVerifyFile in " + sw.getElapsedTimeString());

        System.out.println(String.format("%nProcessing Results"));
        System.out.println(String.format("  Encrypted file name:              %s", result.getEncryptedFileName()));
        System.out.println(String.format("  Decrypted file name:              %s", result.getDecryptedFileName()));
        System.out.println(String.format("  File successfully decrypted:      %s", result.isFileDecrypted()));
        System.out.println(String.format("  File was signed:                  %s", result.isFileSigned()));
        System.out.println(String.format("  File passed signature validation: %s", result.isFilePassedSignatureValidation()));
        System.out.println(String.format("  File was integrity protected:     %s", result.isFileIntegrityProtected()));
        System.out.println(String.format("  File passed integrity validation: %s", result.isFilePassedIntegrityValidation()));

        for (String message : result.getMessages()) {
            System.out.println(String.format("  Processing message: %s", message));
        }
    }
}
