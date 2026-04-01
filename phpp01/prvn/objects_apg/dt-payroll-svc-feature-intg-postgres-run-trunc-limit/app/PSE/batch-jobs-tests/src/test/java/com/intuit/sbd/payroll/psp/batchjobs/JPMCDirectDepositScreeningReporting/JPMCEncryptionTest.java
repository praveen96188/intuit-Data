package com.intuit.sbd.payroll.psp.batchjobs.JPMCDirectDepositScreeningReporting;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriterFactory;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpEncryptedReader;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpEncryptedWriter;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileDecryptionResult;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by charithah418 on 6/14/15.
 */
public class JPMCEncryptionTest {

    private static SpcfLogger logger = SpcfLogManager.getLogger(JPMCEncryptionTest.class);

    @After
    public void afterEachTest() {
        PgpWriterFactory.setInstanceClass(null);
        PgpReaderFactory.setInstanceClass(null);
    }


    @Test
    public void testPGPInMemoryOFACEncryption() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        String rootFolder = new File(".").getCanonicalPath();
        String workingFolder = JPMCEncryptionTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"jpmcscreening/expected/" ;
        String ofacFileName = "OFAC_HappyPath";
        String txtFileName = ofacFileName + ".csv";
        String pgpFileName = ofacFileName + ".pgp";

        StopWatch sw = StopWatch.create(false);

        logger.info("Starting pgpEncrypt...");
        sw.start();
        FileReader fileReader = new FileReader(workingFolder + txtFileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        PgpWriterFactory.setInstanceClass(PgpEncryptedWriter.class);
        PgpWriter pgpWriter = PgpWriterFactory.createInstance();
        pgpWriter.open(workingFolder+txtFileName);
        String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {
            pgpWriter.write(currentLine + "\n");
        }
        pgpWriter.close();
        bufferedReader.close();
        fileReader.close();
        sw.stop();

        logger.info("Encrypted File in location" + workingFolder + pgpFileName);

        logger.info("Completed pgpEncrypt in " + sw.getElapsedTimeString());

        //Decrypt with the intuits test key
        String decKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key");
        String keyPass = "intuit-ach";

        sw.start();

        String decryptFileName = ofacFileName;

        PgpFileDecryptionResult result = PgpFileUtils.pgpDecryptUnsingedFile(workingFolder,
                                                                             pgpFileName,
                                                                             decryptFileName,
                                                                             decKey,
                                                                             keyPass);       // The password for the private key
        sw.stop();


        logger.info("Decrypted File in location" + workingFolder + result.getDecryptedFileName());
        logger.info("Completed pgpDecryptUnsignedFile in " + sw.getElapsedTimeString());

        logger.info(""+ result.getDecryptedFileName());
        assertNotNull("Encrypted file name", result.getEncryptedFileName());
        assertNotNull("Decrypted file name", result.getDecryptedFileName());
        assertTrue("File successfully decrypted", result.isFileDecrypted());
        assertTrue("File is not signed", !result.isFileSigned());
        assertTrue("File was integrity protected", result.isFileIntegrityProtected());
        assertTrue("File passed integrity validation", result.isFilePassedIntegrityValidation());

        assertEquals("Message size", 0, result.getMessages().size());
        for (String message : result.getMessages()) {
            System.out.println(String.format("  Processing message: %s", message));
        }
    }

    @Test
    public void testAMLPGPEncryptionUsingWithoutSignMethod() throws  Exception{
        Security.addProvider(new BouncyCastleProvider());
        String rootFolder = new File(".").getCanonicalPath();
        String workingFolder = JPMCEncryptionTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"jpmcscreening/expected/";

        String fileName = "OFAC_HappyPath";
        String txtFileName = fileName + ".csv";
        String pgpFileName = fileName + ".pgp";

		List<String> encKeyList = new ArrayList<String>();
        encKeyList.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_jpmc_aml_public_key"));

        PgpFileUtils.pgpEncryptWithoutSign(workingFolder,
                                           txtFileName,
                                           pgpFileName,
                                           encKeyList,
                                           true,           // Create ASCII Armor file (Base64 encode the resulting binary stream)
                                           true);          // Include Integrity Packet(s) in the encrypted stream

        File decKeyFile = new File(rootFolder + "/Common/pgp/src/main/resources/intu-test-aml-prv.asc");
        String decKey = FileUtils.readFileToString(decKeyFile, "UTF-8");
        String keyPass = "intuit-aml-test";

        PgpFileDecryptionResult result = PgpFileUtils.pgpDecryptUnsingedFile(workingFolder,
                                                                             pgpFileName,
                                                                             pgpFileName,
                                                                             decKey,
                                                                             keyPass);

        logger.info("Decrypted File in location" + workingFolder + result.getDecryptedFileName());
        assertNotNull("Decrypted file name", result.getDecryptedFileName());
        assertTrue("File successfully decrypted", result.isFileDecrypted());
        assertTrue("File is not signed", !result.isFileSigned());
        assertTrue("File was integrity protected", result.isFileIntegrityProtected());
        assertTrue("File passed integrity validation", result.isFilePassedIntegrityValidation());

        assertEquals("Message size", 0, result.getMessages().size());
        for (String message : result.getMessages()) {
            System.out.println(String.format("  Processing message: %s", message));
        }
    }



    @Test
    public void testPGPEncryptionUsingWithoutSignMethod() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        String rootFolder = new File(".").getCanonicalPath();
        String workingFolder = JPMCEncryptionTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"jpmcscreening/expected/";
        String ofacFileName = "OFAC_HappyPath";
        String txtFileName = ofacFileName + ".csv";
        String pgpFileName = ofacFileName + ".pgp";


        List<String> encKeyList = new ArrayList<String>();
        encKeyList.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key"));

        StopWatch sw = StopWatch.create(false);

        logger.info("Starting pgpEncrypt without SignFile...");

        //Encrypt the file as if the bank did it.
        sw.start();
        PgpFileUtils.pgpEncryptWithoutSign(workingFolder,
                                           txtFileName,
                                           pgpFileName,
                                           encKeyList,
                                           true,           // Create ASCII Armor file (Base64 encode the resulting binary stream)
                                           true);          // Include Integrity Packet(s) in the encrypted stream
        sw.stop();


        logger.info("Encrypted File in location" + workingFolder + pgpFileName);

        logger.info("Completed pgpEncrypt without sign" + sw.getElapsedTimeString());

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.JPMC_SKIP_SIGNATURE_VERIFICATION, "true");
        PayrollServices.commitUnitOfWork();

        //Open csv file for verification
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
}
