package com.intuit.sbd.payroll.psp.common.pgp.impl;

import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.*;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/7/13
 * Time: 10:39 AM
 */
public class PgpEncryptedWriter implements PgpWriter {

    private static final int BUFFER_SIZE = 1 << 16;

    private static final String TXT_FILE_EXT = ".txt";
    private static final String PGP_FILE_EXT = ".pgp";

    private String mEncryptedFileName;
    private String mOriginalFileName;
    private List<String> mEncryptionKeyList;
    private String mSignatureKey;
    private String mSignatureKeyPassword;

    private OutputStream mFileOutputStream;
    private OutputStream mOutputStream;
    private OutputStream mCompressedDataStream;
    private OutputStream mEncryptedDataStream;
    private PGPSignatureGenerator mSignatureGenerator;
    private OutputStream mLiteralOutputStream;
    private OutputStreamWriter mOutputStreamWriter;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void open(String pFileName) throws Exception {
        String path = FilenameUtils.getFullPath(pFileName);
        String fileNameWithOutExt = FilenameUtils.getBaseName(pFileName);

        mOriginalFileName = FilenameUtils.getName(fileNameWithOutExt + TXT_FILE_EXT);
        mEncryptedFileName = path + fileNameWithOutExt + PGP_FILE_EXT;

        try {
            readParameters();
            initialize();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize PgpEncryptedWriter object", e);
        }
    }

    private void readParameters() {
        mEncryptionKeyList = new ArrayList<String>();

        //Add the banks public key so they can decrypt the file
        String key = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_jpmc_public_key");
        if (key != null && key.length() > 0) {
            mEncryptionKeyList.add(key);
        }

        //Add our public key so we can decrypt the file
        key = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key");
        if (key != null && key.length() > 0) {
            mEncryptionKeyList.add(key);
        }

        //Sign the file with our private key
        mSignatureKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key");
        mSignatureKeyPassword = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key_password");
    }

    private void initialize() throws Exception {
        boolean armoredAsciiOutputFile = true;
        boolean withIntegrityCheck = true;

        mFileOutputStream = new BufferedOutputStream(new FileOutputStream(mEncryptedFileName));

        //
        // If want ascii-armored output file, initialize the armored stream, otherwise just use the base file output stream
        //
        mOutputStream = (armoredAsciiOutputFile ? new ArmoredOutputStream(mFileOutputStream) : mFileOutputStream);

        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                                                                                               .setWithIntegrityPacket(withIntegrityCheck)
                                                                                               .setSecureRandom(new SecureRandom())
                                                                                               .setProvider("BC"));
        //add one or more encryption keys
        for (String EncryptionKey: mEncryptionKeyList) {
            PGPPublicKey encKey = PgpFileUtils.readPublicKey(EncryptionKey);
            encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));
        }

        //
        // Initialize the encrypted data stream
        //
        mEncryptedDataStream = encryptedDataGenerator.open(mOutputStream, new byte[BUFFER_SIZE]);

        //
        // Initialize the compressed data stream
        //
        PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
        mCompressedDataStream = compressedDataGenerator.open(mEncryptedDataStream);

        //
        // Initialize the signature generator
        //
        PGPSecretKey pgpSec = PgpFileUtils.readSecretKey(mSignatureKey);
        PGPPrivateKey pgpPrivKey = pgpSec.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(mSignatureKeyPassword.toCharArray()));
        mSignatureGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA1).setProvider("BC"));

        mSignatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

        Iterator iter = pgpSec.getPublicKey().getUserIDs();

        if (iter.hasNext()) {
            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

            spGen.setSignerUserID(false, (String) iter.next());

            mSignatureGenerator.setHashedSubpackets(spGen.generate());
        }

        mSignatureGenerator.generateOnePassVersion(false).encode(mCompressedDataStream);

        //
        // Initialize the literal data generator output stream
        //
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        mLiteralOutputStream = literalDataGenerator.open(mCompressedDataStream,
                                                         PGPLiteralData.BINARY,
                                                         mOriginalFileName,
                                                         new Date(new File(mEncryptedFileName).lastModified()),
                                                         new byte[BUFFER_SIZE]);

        mOutputStreamWriter = new OutputStreamWriter(mLiteralOutputStream);
    }

    public void write(String pString) throws IOException {
        try {
            mOutputStreamWriter.write(pString);
            mSignatureGenerator.update(pString.getBytes());
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void write(char pChar) throws IOException {
        try {
            mOutputStreamWriter.write(pChar);
            mSignatureGenerator.update((byte) pChar);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void flush() throws IOException {
        mOutputStreamWriter.flush();
    }

    public void close() throws IOException {
        try {
            if (mOutputStreamWriter != null)
                mOutputStreamWriter.close();
            if (mLiteralOutputStream != null)
                mLiteralOutputStream.close();
            if (mSignatureGenerator != null)
                mSignatureGenerator.generate().encode(mCompressedDataStream);
            if (mCompressedDataStream != null)
                mCompressedDataStream.close();
            if (mEncryptedDataStream != null)
                mEncryptedDataStream.close();
            if (mOutputStream != null)
                mOutputStream.close();
            if (mFileOutputStream != null)
                mFileOutputStream.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

}
