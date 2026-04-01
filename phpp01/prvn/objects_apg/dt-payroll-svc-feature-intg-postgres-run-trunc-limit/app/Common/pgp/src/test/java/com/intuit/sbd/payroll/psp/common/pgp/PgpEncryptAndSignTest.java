package com.intuit.sbd.payroll.psp.common.pgp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpEncryptedReader;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpEncryptedWriter;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileDecryptionResult;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import java.io.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: kpaul
 * Date: 12/6/12
 * Time: 11:11 PM
 * To change this template use File | Settings | File Templates.
 */


public class PgpEncryptAndSignTest {

    @After
    public void afterEachTest() {
        PgpWriterFactory.setInstanceClass(null);
        PgpReaderFactory.setInstanceClass(null);
    }

    @Test
    public void testInMemoryEncryption() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        //
        // This will test the PGP encryption and signing of a file, producing a PGP Ascii-Armored message file
        //
        // - We need to encrypt the file using the Bank's and Intuit's public key (the Bank or Intuit will decrypt the file using their private key).
        // - We need to sign the file using Intuit's private key (the bank will validate the signature using Intuit's public key).
        //
        // > This will ensure our encryption and signing code are working properly.
        // > The encrypted+signed file produced here should be compatible with all PGP/GPG tools and utilities.
        //

        String rootFolder = new File(".").getCanonicalPath();
        String workingFolder = PgpEncryptAndSignTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String achFileName = "d.1359767724043CCD";
        String txtFileName = achFileName + ".txt";
        String pgpFileName = achFileName + ".pgp";

        PgpFileUtils.deleteFile(workingFolder + txtFileName);
        PgpFileUtils.deleteFile(workingFolder + pgpFileName);
        PgpFileUtils.copyFile(rootFolder + "/Common/pgp/src/test/resources/orig/" + txtFileName, workingFolder);

        StopWatch sw = StopWatch.create(false);

        System.out.println("Starting pgpEncryptAndSignFile...");

        sw.start();
        FileReader fileReader = new FileReader(workingFolder + txtFileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        PgpWriterFactory.setInstanceClass(PgpEncryptedWriter.class);
        PgpWriter pgpWriter = PgpWriterFactory.createInstance();
        pgpWriter.open(workingFolder + txtFileName);
        String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {
            pgpWriter.write(currentLine + "\n");
        }
        pgpWriter.close();
        bufferedReader.close();
        fileReader.close();
        sw.stop();

        System.out.println("Completed pgpEncryptAndSignFile in " + sw.getElapsedTimeString());

        //Decrypt with the banks test key
        File decKeyFile = new File(rootFolder + "/Common/pgp/src/main/resources/bank-test-prv-20141206.asc");
        String decKey = FileUtils.readFileToString(decKeyFile, "UTF-8");
        String sigKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key");
        String keyPass = "jpmc-ach";

        sw.start();
        PgpFileDecryptionResult result = PgpFileUtils.pgpDecryptAndVerifyFile(workingFolder,
                                                                              pgpFileName,    // The encrypted+signed file to be decrypted and validated
                                                                              txtFileName,    // The preferred 'default' file name for the decrypted file
                                                                              sigKey, // The Intuit Public Key file (used to verify the signature of the signed file)
                                                                              decKey, // The JPMC Private Key file (used to decrypt the encrypted file)
                                                                              keyPass);       // The password for the private key
        sw.stop();

        System.out.println("Completed pgpDecryptAndVerifyFile in " + sw.getElapsedTimeString());

        assertNotNull("Encrypted file name", result.getEncryptedFileName());
        assertNotNull("Decrypted file name", result.getDecryptedFileName());
        assertTrue("File successfully decrypted", result.isFileDecrypted());
        assertTrue("File was signed", result.isFileSigned());
        assertTrue("File passed signature validation", result.isFilePassedSignatureValidation());
        assertTrue("File was integrity protected", result.isFileIntegrityProtected());
        assertTrue("File passed integrity validation", result.isFilePassedIntegrityValidation());

        assertEquals("Message size", 0, result.getMessages().size());
        for (String message : result.getMessages()) {
            System.out.println(String.format("  Processing message: %s", message));
        }

        //Decrypt with the intuits test key
        decKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key");
        sigKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key");
        keyPass = "intuit-ach";

        sw.start();
        result = PgpFileUtils.pgpDecryptAndVerifyFile(workingFolder,
                                                      pgpFileName,    // The encrypted+signed file to be decrypted and validated
                                                      txtFileName,    // The preferred 'default' file name for the decrypted file
                                                      sigKey, // The Intuit Public Key file (used to verify the signature of the signed file)
                                                      decKey, // The JPMC Private Key file (used to decrypt the encrypted file)
                                                      keyPass);       // The password for the private key
        sw.stop();

        System.out.println("Completed pgpDecryptAndVerifyFile in " + sw.getElapsedTimeString());

        assertNotNull("Encrypted file name", result.getEncryptedFileName());
        assertNotNull("Decrypted file name", result.getDecryptedFileName());
        assertTrue("File successfully decrypted", result.isFileDecrypted());
        assertTrue("File was signed", result.isFileSigned());
        assertTrue("File passed signature validation", result.isFilePassedSignatureValidation());
        assertTrue("File was integrity protected", result.isFileIntegrityProtected());
        assertTrue("File passed integrity validation", result.isFilePassedIntegrityValidation());

        assertEquals("Message size", 0, result.getMessages().size());
        for (String message : result.getMessages()) {
            System.out.println(String.format("  Processing message: %s", message));
        }
    }

    @Test
    public void testInMemoryDecryption() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        String rootFolder = new File(".").getCanonicalPath();
        String workingFolder = PgpEncryptAndSignTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String achFileName = "d.1359767724043CCD";
        String txtFileName = achFileName + ".txt";
        String pgpFileName = achFileName + ".pgp";

        List<String> encKeyList = new ArrayList<String>();
        encKeyList.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_jpmc_public_key"));
        encKeyList.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key"));

        File sigKeyFile = new File(rootFolder + "/Common/pgp/src/main/resources/bank-test-prv-20141206.asc");
        String sigKey = FileUtils.readFileToString(sigKeyFile, "UTF-8");
        String keyPass = "jpmc-ach";

        PgpFileUtils.deleteFile(workingFolder + txtFileName);
        PgpFileUtils.deleteFile(workingFolder + pgpFileName);
        PgpFileUtils.copyFile(rootFolder + "/Common/pgp/src/test/resources/orig/" + txtFileName, workingFolder);

        StopWatch sw = StopWatch.create(false);

        System.out.println("Starting pgpEncryptAndSignFile...");

        //Encrypt the file as if the bank did it.
        sw.start();
        PgpFileUtils.pgpEncryptAndSignFile(workingFolder,
                                           txtFileName,    // ACH file to be encrypted
                                           pgpFileName,    // Output file name for the encrypted+signed ACH file
                                           encKeyList, // The JPMC Public Key file (used to encrypt the ACH file)
                                           sigKey, // The Intuit Private Key file (used to sign the ACH file)
                                           keyPass,        // The password for the private key
                                           true,           // Create ASCII Armor file (Base64 encode the resulting binary stream)
                                           true);          // Include Integrity Packet(s) in the encrypted stream
        sw.stop();
        System.out.println("Completed pgpEncryptAndSignFile in " + sw.getElapsedTimeString());

        //Open txt file for verification
        BufferedReader bufferedReader = new BufferedReader(new FileReader(workingFolder + txtFileName));

        //Decrypt the file as if Intuit is doing it.
        PgpReaderFactory.setInstanceClass(PgpEncryptedReader.class);
        PgpReader pgpReader = PgpReaderFactory.createInstance();
        pgpReader.open(workingFolder + pgpFileName);

        int lineNumber = 0;
        String currentLine;
        while ((currentLine = pgpReader.readLine()) != null) {
            lineNumber++;
            assertEquals(String.format("Mismatch on line %d", lineNumber), bufferedReader.readLine(), currentLine);
        }

        pgpReader.close();
        bufferedReader.close();
    }


    @Test
    public void testInMemoryDecryptionSkipVerify() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        String rootFolder = new File(".").getCanonicalPath();
        String workingFolder = PgpEncryptAndSignTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String achFileName = "d.1359767724043CCD";
        String txtFileName = achFileName + ".txt";
        String pgpFileName = achFileName + ".pgp";

        List<String> encKeyList = new ArrayList<String>();
        encKeyList.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_jpmc_public_key"));
        encKeyList.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key"));

        String sigKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key");
        String keyPass = "intuit-ach";

        PgpFileUtils.deleteFile(workingFolder + txtFileName);
        PgpFileUtils.deleteFile(workingFolder + pgpFileName);
        PgpFileUtils.copyFile(rootFolder + "/Common/pgp/src/test/resources/orig/" + txtFileName, workingFolder);

        StopWatch sw = StopWatch.create(false);

        System.out.println("Starting pgpEncryptAndSignFile...");

        //Encrypt the file as if the bank did it.
        sw.start();
        PgpFileUtils.pgpEncryptAndSignFile(workingFolder,
                                           txtFileName,    // ACH file to be encrypted
                                           pgpFileName,    // Output file name for the encrypted+signed ACH file
                                           encKeyList, // The JPMC Public Key file (used to encrypt the ACH file)
                                           sigKey, // The Intuit Private Key file (used to sign the ACH file)
                                           keyPass,        // The password for the private key
                                           true,           // Create ASCII Armor file (Base64 encode the resulting binary stream)
                                           true);          // Include Integrity Packet(s) in the encrypted stream
        sw.stop();
        System.out.println("Completed pgpEncryptAndSignFile in " + sw.getElapsedTimeString());

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.JPMC_SKIP_SIGNATURE_VERIFICATION, "true");
        PayrollServices.commitUnitOfWork();

        //Open txt file for verification
        BufferedReader bufferedReader = new BufferedReader(new FileReader(workingFolder + txtFileName));

        //Decrypt the file as if Intuit is doing it.
        PgpReaderFactory.setInstanceClass(PgpEncryptedReader.class);
        PgpReader pgpReader = PgpReaderFactory.createInstance();
        pgpReader.open(workingFolder + pgpFileName);

        int lineNumber = 0;
        String currentLine;
        while ((currentLine = pgpReader.readLine()) != null) {
            lineNumber++;
            assertEquals(String.format("Mismatch on line %d", lineNumber), bufferedReader.readLine(), currentLine);
        }

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.JPMC_SKIP_SIGNATURE_VERIFICATION, "false");
        PayrollServices.commitUnitOfWork();

        pgpReader.close();
        bufferedReader.close();
    }
}
