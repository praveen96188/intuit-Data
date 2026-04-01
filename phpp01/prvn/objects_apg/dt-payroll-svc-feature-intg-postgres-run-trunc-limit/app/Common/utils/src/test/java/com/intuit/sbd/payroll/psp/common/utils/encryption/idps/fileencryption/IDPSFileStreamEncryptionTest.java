package com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption;

import com.amazonaws.util.StringInputStream;
import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IdpsSdkConstants;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;
import java.math.BigDecimal;
import static org.junit.Assert.*;

public class IDPSFileStreamEncryptionTest {

    private static IdpsClient idpsClient ;

    public static final Logger logger = Logger.getLogger(IDPSFileStreamEncryptionTest.class.getName());

    private static String keyName = "PSP/" + "TestJob" + "AES256_GCM";

    public static IdpsClient getIdpsClient() {
        return idpsClient;
    }
    static {
        ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration("PSP-Keys");
        String apiKeyId = config.getString("psp_idps_api_key_id");
        String apiSecretKey = config.getString("psp_idps_api_secret_key");
        String apiPolicy = config.getString("psp_idps_api_policy");
        String accessType = config.getString("psp_idps_access_type");
        String endpoint = config.getString("psp_idps_endpoint");

        Properties idpsProperties = new Properties();
        idpsProperties.setProperty("endpoint", endpoint);
        if(!apiPolicy.isEmpty()){
            logger.info("apiPolicy : " + apiPolicy);
            idpsProperties.setProperty("policy_id", apiPolicy);
            if (StringUtils.isNotBlank(accessType)) {
                idpsProperties.setProperty("access_type", accessType);
            }
        } else {
            idpsProperties.setProperty("api_key_id", apiKeyId);
            idpsProperties.setProperty("api_secret_key", apiSecretKey);
        }

        try {
            idpsClient = IdpsClient.Factory.newInstance(idpsProperties);
            idpsClient.setCryptoLocation(IdpsClient.CryptoLocation.LOCAL);
        } catch (IdpsException | IOException e) {
            logger.info("Exception : "+e.getMessage());
            e.printStackTrace();
        } catch (Exception exp){
            logger.info("Exception : "+exp.getMessage());
            exp.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void encryptFileWriter()
    {
        Key key = idpsClient.newKeyHandleLatest(keyName);


        try {

            BigDecimal b = new BigDecimal(274674.434);
            IDPSFileWriter secWriter = new IDPSFileWriter( new File("encrypted_filewritertest.bytes"), true, key);
            FileWriter unsecWriter = new FileWriter(new File("unencryptedfilewriter.txt"));
            //secWriter.write(String.valueOf(mNewBatchId));
            secWriter.write("Test");
            unsecWriter.write("Test");
            secWriter.write("Activity");
            unsecWriter.write("Activity");
            secWriter.write(";");
            unsecWriter.write(";");
            secWriter.write("Bank");
            unsecWriter.write("Bank");
            secWriter.write(";");
            unsecWriter.write(";");
            secWriter.write("sku");
            unsecWriter.write("sku");
            secWriter.write(";");
            unsecWriter.write(";");
            secWriter.write(String.format("%.2f", b));
            unsecWriter.write(String.format("%.2f", b));


            secWriter.flush();
            secWriter.close();
            unsecWriter.close();

            //compare with decrypted file and unencrypted file

            BufferedReader reader1 = new BufferedReader(new IDPSFileReader(new File("encrypted_filewritertest.bytes"),key));

            BufferedReader reader2 = new BufferedReader( new FileReader(new File("unencryptedfilewriter.txt")));
            String line1 = reader1.readLine();

            String line2 = reader2.readLine();

            boolean areEqual = true;

            int lineNum = 1;

            while (line1 != null || line2 != null)
            {
                if(line1 == null || line2 == null)
                {
                    areEqual = false;

                    break;
                }
                else if(! line1.equalsIgnoreCase(line2))
                {
                    areEqual = false;

                    break;
                }

                line1 = reader1.readLine();

                line2 = reader2.readLine();

                lineNum++;
            }

            assertEquals(areEqual,true);
            reader1.close();
            reader2.close();

        } catch (Exception ex) {
        }
        finally {

        }
    }

    @Test
    public void testOutputStreamFlush() throws Exception {

        Key key = idpsClient.newKeyHandleLatest(keyName);
        IDPSFileOutputStream os = new IDPSFileOutputStream( "test",key);
        byte[] bytes = null;
        int initialByteSizeToWrite = 10;

        // Write 10 bytes
        os.write(bytes = RandomStringUtils.randomNumeric(initialByteSizeToWrite).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite, os.getBufferCount());

        // Write one chunk
        os.write(bytes = RandomStringUtils.randomNumeric(IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite, os.getBufferCount());

        // Write one chunk
        os.write(bytes = RandomStringUtils.randomNumeric(IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite, os.getBufferCount());

        // Write two chunks + initialByteSizeToWrite
        os.write(bytes = RandomStringUtils.randomNumeric((IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE * 2) + initialByteSizeToWrite).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite * 2, os.getBufferCount());

        os.endWriting();
        Assert.assertEquals("Bytes not written to the stream properly", 0, os.getBufferCount());
    }

    @Test
    public void testOutputStreamFlushEncryptionTurnedOff() throws Exception {

        Key key = idpsClient.newKeyHandleLatest(keyName);
        IDPSFileOutputStream os = new IDPSFileOutputStream( "test",key);
        byte[] bytes = null;
        int initialByteSizeToWrite = 0;

        // Write 10 bytes
        os.write(bytes = RandomStringUtils.randomNumeric(initialByteSizeToWrite).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite, os.getBufferCount());

        // Write one chunk
        os.write(bytes = RandomStringUtils.randomNumeric(IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite, os.getBufferCount());

        // Write one chunk
        os.write(bytes = RandomStringUtils.randomNumeric(IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite, os.getBufferCount());

        // Write two chunks + initialByteSizeToWrite
        os.write(bytes = RandomStringUtils.randomNumeric((IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE * 2) + initialByteSizeToWrite).getBytes());
        Assert.assertEquals("Bytes not written to the stream properly", initialByteSizeToWrite * 2, os.getBufferCount());

        os.endWriting();
        Assert.assertEquals("Bytes not written to the stream properly", 0, os.getBufferCount());
    }
}