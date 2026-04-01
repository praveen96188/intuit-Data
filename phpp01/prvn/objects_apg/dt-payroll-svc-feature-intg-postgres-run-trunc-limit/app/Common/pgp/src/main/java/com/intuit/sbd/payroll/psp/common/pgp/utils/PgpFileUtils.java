package com.intuit.sbd.payroll.psp.common.pgp.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.*;

import java.io.*;
import java.security.SecureRandom;
import java.security.Security;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kpaul
 * Date: 12/12/12
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class PgpFileUtils {
    private static final int BUFFER_SIZE = 1 << 16;
    // Provided by AP Intego Team
    private static final int MAX_ITERATIONS = 10000;
    private static List<String> mEncryptionKeyList;
    private static String mSignatureKey;
    private static String mSignatureKeyPassword;
    private static String mDecryptionKey;
    private static String mDecryptionKeyPassword;

    public static void main(String[] args) {

        try {
            Commands command;
            FileOriginators fileOriginator;
            String inputFile;
            String outputFile;

            if (args.length == 4) {
                command = Commands.valueOf(args[0]);
                fileOriginator = FileOriginators.valueOf(args[1]);
                inputFile = args[2];
                outputFile = args[3];
            } else {
                throw new Exception("usage");
            }

            Application.initialize();
            ApplicationSecondary.initialize();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));
            Application.beginUnitOfWork();

            String path;
            switch (command) {
                case Decrypt:
                    readDecryptionParameters(fileOriginator);

                    path = FilenameUtils.getFullPath(inputFile);
                    inputFile = FilenameUtils.getName(inputFile);
                    outputFile = FilenameUtils.getName(outputFile);

                    PgpFileDecryptionResult result = PgpFileUtils.pgpDecryptAndVerifyFile(path,
                                                                                          inputFile,    // The encrypted+signed file to be decrypted and validated
                                                                                          outputFile,    // The preferred 'default' file name for the decrypted file
                                                                                          mSignatureKey, // The Intuit Public Key file (used to verify the signature of the signed file)
                                                                                          mDecryptionKey, // The JPMC Private Key file (used to decrypt the encrypted file)
                                                                                          mDecryptionKeyPassword);       // The password for the private key

                    System.out.println("Completed pgpDecryptAndVerifyFile.");

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

                    break;
                case Encrypt:
                    readEncryptionParameters();
                    path = FilenameUtils.getFullPath(inputFile);
                    inputFile = FilenameUtils.getName(inputFile);
                    outputFile = FilenameUtils.getName(outputFile);

                    PgpFileUtils.pgpEncryptAndSignFile(path,
                                                       inputFile,    // ACH file to be encrypted
                                                       outputFile,    // Output file name for the encrypted+signed ACH file
                                                       mEncryptionKeyList, // The JPMC Public Key file (used to encrypt the ACH file)
                                                       mSignatureKey, // The Intuit Private Key file (used to sign the ACH file)
                                                       mSignatureKeyPassword,        // The password for the private key
                                                       true,           // Create ASCII Armor file (Base64 encode the resulting binary stream)
                                                       true);          // Include Integrity Packet(s) in the encrypted stream

                    break;
            }

            Application.rollbackUnitOfWork();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equals("usage")) {
                usage();
            } else {
                e.printStackTrace();
            }
        }
    }

    private static void readDecryptionParameters(FileOriginators pFileOriginator) {
        //Intuits private key and Password to decrypt the file
        mDecryptionKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key");
        mDecryptionKeyPassword = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key_password");

        switch (pFileOriginator) {
            case Intuit:
                //Verify with the Intuit public key
                mSignatureKey= ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key");
                break;
            case Bank:
                //Verify with the banks public key
                mSignatureKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_jpmc_public_key");
                break;
        }


    }

    public static void readEncryptionParameters() {
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

    private static void usage() {
        System.out.println("Usage: PgpFileUtils <Command> <File Originator> <Input File> <Output File>");
        System.out.println("Valid commands are " + Arrays.toString(Commands.values()));
        System.out.println("Valid file originators are " + Arrays.toString(FileOriginators.values()));
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static boolean deleteFile(String pFileName) {
        File file = new File(pFileName);
        return !file.exists() || file.delete();
    }

    public static File copyFile(String pSourceFile, String pDestDir) {
        return copyFile(new File(pSourceFile), pDestDir);
    }

    public static File copyFile(File pSourceFile, String pDestDir) {
        return copyFile(pSourceFile, pDestDir, null);
    }

    public static File copyFile(File pSourceFile, String pDestDir, String pCharsetName) {
        try {
            File destFile = new File(pDestDir, pSourceFile.getName());
            BufferedReader reader;
            BufferedWriter writer;

            if (pCharsetName != null) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(pSourceFile), pCharsetName));
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), pCharsetName));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(pSourceFile)));
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile)));
            }

            try {
                char buf[] = new char[1024];
                int bytesRead;

                while ((bytesRead = reader.read(buf)) > 0) {
                    writer.write(buf, 0, bytesRead);
                }

                writer.flush();
            } finally {
                writer.close();
                reader.close();
            }

            return destFile;
        } catch (Throwable t) {
            String msg = String.format("Error copying file (source: %s, dest: %s)", pSourceFile.getPath(), pDestDir);
            throw new RuntimeException(msg, t);
        }
    }

    /**
     * Reads a public key from the given file (certificate file or key ring).
     *
     * @param pKey The name of the file containing the desired public key.
     * @return The first public key that can be used for file encryption.
     * @throws IOException
     * @throws PGPException
     */
    public static PGPPublicKey readPublicKey(String pKey) throws IOException, PGPException {
        InputStream keyIn = new ByteArrayInputStream(pKey.getBytes());
        try {
            return readPublicKey(keyIn);
        } finally {
            keyIn.close();
        }
    }

    /**
     * A simple routine that opens a key ring file and loads the first available key
     * suitable for encryption.
     *
     * @param pInputStream Input stream to read the public key ring collection from.
     * @return The first public key that can be used for file encryption.
     * @throws IOException
     * @throws PGPException
     */
    public static PGPPublicKey readPublicKey(InputStream pInputStream) throws IOException, PGPException {
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(pInputStream),new JcaKeyFingerprintCalculator());

        //
        // We normally just pass a key file into this method (as opposed to a key ring file),
        // so PGPPublicKeyRingCollection will typically only have one member. If we pass in a
        // key ring file we probably want to use a little more discretion on selecting the key.
        //
        // Just loop through the collection till we find a key suitable for encryption.
        //

        Iterator keyRingIter = pgpPub.getKeyRings();

        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();
            Iterator keyIter = keyRing.getPublicKeys();

            while (keyIter.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) keyIter.next();

                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }

    public static List<PGPPublicKey> readMultiplePublicKeys(InputStream inputStream, int maxIterations) throws Exception {
        List<PGPPublicKey> keys = new ArrayList<>();

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(inputStream),new JcaKeyFingerprintCalculator());

        //
        // We normally just pass a key file into this method (as opposed to a key ring file),
        // so PGPPublicKeyRingCollection will typically only have one member. If we pass in a
        // key ring file we probably want to use a little more discretion on selecting the key.
        //
        // Just loop through the collection till we find a key suitable for encryption.
        //

        Iterator keyRingIter = pgpPub.getKeyRings();

        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();
            Iterator keyIter = keyRing.getPublicKeys();

            while (keyIter.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) keyIter.next();

                if (!key.isEncryptionKey()) {
                    continue;
                }
                keys.add(key);
            }
        }

        if (keys.isEmpty()) {
            throw new IOException("Loop exceeded max iteration count.");
        } else {
            return keys;
        }
    }


    /**
     * Reads a private key from the given file (certificate file or key ring).
     *
     * @param pKey The name of the file containing the desired private key.
     * @return The first private key that can be used for digitally signing a file.
     * @throws IOException
     * @throws PGPException
     */
    public static PGPSecretKey readSecretKey(String pKey) throws IOException, PGPException {
        InputStream keyIn = new BufferedInputStream(new ByteArrayInputStream(pKey.getBytes()));

        try {
            return readSecretKey(keyIn);
        } finally {
            keyIn.close();
        }
    }

    /**
     * A simple routine that opens a key ring file and loads the first available key
     * suitable for signature generation.
     *
     * @param pInputStream Input stream to read the secret key ring collection from.
     * @return The first secret key that can be used for file encryption.
     * @throws IOException
     * @throws PGPException
     */
    public static PGPSecretKey readSecretKey(InputStream pInputStream) throws IOException, PGPException {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(pInputStream),new JcaKeyFingerprintCalculator());

        //
        // We normally just pass a key file into this method (as opposed to a key ring file),
        // so PGPPublicKeyRingCollection will typically only have one member. If we pass in a
        // key ring file we probably want to use a little more discretion on selecting the key.
        //
        // Just loop through the collection till we find a key suitable for signing.
        //

        Iterator keyRingIter = pgpSec.getKeyRings();

        while (keyRingIter.hasNext()) {
            PGPSecretKeyRing keyRing = (PGPSecretKeyRing) keyRingIter.next();
            Iterator keyIter = keyRing.getSecretKeys();

            while (keyIter.hasNext()) {
                PGPSecretKey key = (PGPSecretKey) keyIter.next();

                if (key.isSigningKey()) {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException("Can't find signing key in key ring.");
    }



    /**
     * Method to encrypt and sign a file.
     *
     * @param pInputFileName             The file to be encrypted
     * @param pOutputFileName            Output file name for the encrypted+signed file
     * @param pEncryptionKeyList List if Public Key files (used to encrypt the file)
     * @param pSignatureKey      The Private Key file (used to sign the file)
     * @param pSignatureKeyPassword      The password for the private key
     * @param pArmoredAsciiOutputFile    Create ASCII Armor file (Base64 encode the resulting binary stream and format as PGP message)
     * @param pWithIntegrityCheck        Include Integrity Packet(s) in the encrypted stream
     * @throws Exception
     */
    public static void pgpEncryptAndSignFile(String pWorkingFolder,
                                             String pInputFileName,
                                             String pOutputFileName,
                                             List<String> pEncryptionKeyList,
                                             String pSignatureKey,
                                             String pSignatureKeyPassword,
                                             boolean pArmoredAsciiOutputFile,
                                             boolean pWithIntegrityCheck) throws Exception {
        OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(pWorkingFolder + pOutputFileName));

        try {
            //
            // If want ascii-armored output file, initialize the armored stream, otherwise just use the base file output stream
            //
            OutputStream outputStream = (pArmoredAsciiOutputFile ? new ArmoredOutputStream(fileOutputStream) : fileOutputStream);

            try {
                //
                // Initialize the encrypted data stream
                //

                PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                                                                                                         .setWithIntegrityPacket(pWithIntegrityCheck)
                                                                                                         .setSecureRandom(new SecureRandom())
                                                                                                         .setProvider("BC"));

                //add one or more encryption keys
                for (String EncryptionKey : pEncryptionKeyList) {
                    PGPPublicKey encKey = readPublicKey(EncryptionKey);
                    encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));
                }

                OutputStream encryptedDataStream = encryptedDataGenerator.open(outputStream, new byte[BUFFER_SIZE]);

                try {
                    //
                    // Initialize the compressed data stream
                    //
                    PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
                    OutputStream compressedDataStream = compressedDataGenerator.open(encryptedDataStream);

                    try {
                        //
                        // Initialize the signature generator
                        //
                        PGPSecretKey pgpSec = readSecretKey(pSignatureKey);
                        PGPPrivateKey pgpPrivKey = pgpSec.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pSignatureKeyPassword.toCharArray()));
                        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(pgpSec.getPublicKey().getAlgorithm(),
                                                                                                                            PGPUtil.SHA1).setProvider("BC"));

                        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

                        Iterator iter = pgpSec.getPublicKey().getUserIDs();

                        if (iter.hasNext()) {
                            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

                            spGen.setSignerUserID(false, (String) iter.next());

                            signatureGenerator.setHashedSubpackets(spGen.generate());
                        }

                        signatureGenerator.generateOnePassVersion(false).encode(compressedDataStream);

                        //
                        // Initialize the literal data generator output stream
                        //
                        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
                        OutputStream literalOutputStream = literalDataGenerator.open(compressedDataStream,
                                                                                     PGPLiteralData.BINARY,
                                                                                     pInputFileName,
                                                                                     new Date(new File(pOutputFileName).lastModified()),
                                                                                     new byte[BUFFER_SIZE]);

                        try {
                            //
                            // Read input file and write to target file using a buffer
                            //
                            InputStream inputStream = new FileInputStream(pWorkingFolder + pInputFileName);

                            try {
                                byte[] buf = new byte[BUFFER_SIZE];
                                int len;

                                while ((len = inputStream.read(buf, 0, buf.length)) > 0) {
                                    literalOutputStream.write(buf, 0, len);
                                    signatureGenerator.update(buf, 0, len);
                                }
                            } finally {
                                inputStream.close();
                            }
                        } finally {
                            literalOutputStream.close();
                        }

                        signatureGenerator.generate().encode(compressedDataStream);
                    } finally {
                        compressedDataStream.close();
                    }
                } finally {
                    encryptedDataStream.close();
                }
            } finally {
                if (pArmoredAsciiOutputFile) {
                    outputStream.close();
                }
            }
        } finally {
            fileOutputStream.close();
        }
    }

    /**
     * @param pWorkingFolder
     * @param pInputFileName
     * @param pOutputFileName            encrypted unsigned file
     * @param pEncryptionKeyList
     * @param pArmoredAsciiOutputFile
     * @param pWithIntegrityCheck
     * @throws Exception
     */

    public static void pgpEncryptWithoutSign(String pWorkingFolder,
                                             String pInputFileName,
                                             String pOutputFileName,
                                             List<String> pEncryptionKeyList,
                                             boolean pArmoredAsciiOutputFile,
                                             boolean pWithIntegrityCheck) throws Exception {


        OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(pWorkingFolder + pOutputFileName));


        //
        // If want ascii-armored output file, initialize the armored stream, otherwise just use the base file output stream
        //
        OutputStream outputStream = (pArmoredAsciiOutputFile ? new ArmoredOutputStream(fileOutputStream) : fileOutputStream);


        //
        // Initialize the encrypted data stream
        //

        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                                                                                                 .setWithIntegrityPacket(pWithIntegrityCheck)
                                                                                                 .setSecureRandom(new SecureRandom())
                                                                                                 .setProvider("BC"));

        //add one or more encryption keys
        for (String EncryptionKey : pEncryptionKeyList) {
            PGPPublicKey encKey = readPublicKey(EncryptionKey);
            encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));
        }

        OutputStream encryptedDataStream = encryptedDataGenerator.open(outputStream, new byte[BUFFER_SIZE]);


        //
        // Initialize the compressed data stream
        //
        PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
        OutputStream compressedDataStream = compressedDataGenerator.open(encryptedDataStream);

        //
        // Initialize the literal data generator output stream
        //
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        OutputStream literalOutputStream = literalDataGenerator.open(compressedDataStream,
                                                                     PGPLiteralData.BINARY,
                                                                     pInputFileName,
                                                                     new Date(new File(pOutputFileName).lastModified()),
                                                                     new byte[BUFFER_SIZE]);


        //
        // Read input file and write to target file using a buffer
        //
        InputStream inputStream = new FileInputStream(pWorkingFolder + pInputFileName);


        byte[] buf = new byte[BUFFER_SIZE];
        int len;

        while ((len = inputStream.read(buf, 0, buf.length)) > 0) {
            literalOutputStream.write(buf, 0, len);
        }


        if (inputStream != null) {
            inputStream.close();
        }
        if (literalOutputStream != null) {
            literalOutputStream.close();
        }
        if (compressedDataStream != null) {
            compressedDataStream.close();
        }
        if (encryptedDataStream != null) {
            encryptedDataStream.close();
        }
        if (pArmoredAsciiOutputFile && outputStream != null) {
            outputStream.close();
        }
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }

    }

    /**
     * NOTE: We are using this method for AP Intego workers comp pgp encryption purpose only.
     *
     * It uses multiple keys for encryption.
     *
     * @param pInputFileName
     * @param pOutputFileName
     * @param encryptionKeyInputStream
     * @param pArmoredAsciiOutputFile
     * @param pWithIntegrityCheck
     * @throws Exception
     */
    public static void pgpEncryptWithoutSignWithMultipleKeys(File pInputFileName,
                                                             File pOutputFileName,
                                                             InputStream encryptionKeyInputStream,
                                                             boolean pArmoredAsciiOutputFile,
                                                             boolean pWithIntegrityCheck) throws Exception {

        OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(pOutputFileName));

        //
        // If want ascii-armored output file, initialize the armored stream,
        // otherwise just use the base file output stream
        //
        OutputStream outputStream = (pArmoredAsciiOutputFile ? new ArmoredOutputStream(fileOutputStream) : fileOutputStream);

        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                .setWithIntegrityPacket(pWithIntegrityCheck)
                .setSecureRandom(new SecureRandom())
                .setProvider("BC"));


        List<PGPPublicKey> encKeys = readMultiplePublicKeys(encryptionKeyInputStream, MAX_ITERATIONS);

        // add one or more encryption keys
        for (PGPPublicKey key : encKeys) {
            encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(key).setProvider("BC"));
        }

        OutputStream encryptedDataStream = encryptedDataGenerator.open(outputStream, new byte[BUFFER_SIZE]);

        //
        // Initialize the compressed data stream
        //
        PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
        OutputStream compressedDataStream = compressedDataGenerator.open(encryptedDataStream);

        //
        // Initialize the literal data generator output stream
        //
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        OutputStream literalOutputStream = literalDataGenerator.open(compressedDataStream,
                PGPLiteralData.BINARY,
                pInputFileName.getAbsolutePath(),
                new Date(pOutputFileName.lastModified()),
                new byte[BUFFER_SIZE]);

        //
        // Read input file and write to target file using a buffer
        //
        InputStream inputStream = new FileInputStream(pInputFileName);

        byte[] buf = new byte[BUFFER_SIZE];
        int len;

        while ((len = inputStream.read(buf, 0, buf.length)) > 0) {
            literalOutputStream.write(buf, 0, len);
        }

        if (inputStream != null) {
            inputStream.close();
        }
        if (literalOutputStream != null) {
            literalOutputStream.close();
        }
        if (compressedDataStream != null) {
            compressedDataStream.close();
        }
        if (encryptedDataStream != null) {
            encryptedDataStream.close();
        }
        if (pArmoredAsciiOutputFile && outputStream != null) {
            outputStream.close();
        }
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }

    }

    /**
     * Method to decrypt and verify a file.
     *
     * @param pInputFileName         The encrypted+signed file to be decrypted and validated
     * @param pDefaultOutputFileName The preferred 'default' file name for the decrypted file. Normally the file name to be used for the output (decrypted) file
     *                               is contained in the body of the PGP message, however if it is not found there then this default file name will be used.
     * @param pSignatureKey  The Public Key file (used to verify the signature of the signed file)
     * @param pDecryptionKey The Private Key file (used to decrypt the encrypted file)
     * @param pDecryptionKeyPassword The password for the private key
     * @return PgpFileDecryptionResult containing decryption and signature verification results (and, in some cases, messages describing any problems)
     * @throws Exception
     */
    public static PgpFileDecryptionResult pgpDecryptAndVerifyFile(String pWorkingFolder,
                                                                  String pInputFileName,
                                                                  String pDefaultOutputFileName,
                                                                  String pSignatureKey,
                                                                  String pDecryptionKey,
                                                                  String pDecryptionKeyPassword) throws Exception {
        PgpFileDecryptionResult result = new PgpFileDecryptionResult();

        result.setEncryptedFileName(pWorkingFolder + pInputFileName);

        try {
            InputStream inputStream = PGPUtil.getDecoderStream(new BufferedInputStream(new FileInputStream(pWorkingFolder + pInputFileName)));

            try {
                PGPObjectFactory pgpF = new PGPObjectFactory(inputStream, new JcaKeyFingerprintCalculator());
                PGPEncryptedDataList enc;

                //
                // The first object might be a PGP marker packet, in which case just skip it.
                //
                Object o = pgpF.nextObject();

                if (o instanceof PGPEncryptedDataList) {
                    enc = (PGPEncryptedDataList) o;
                } else {
                    enc = (PGPEncryptedDataList) pgpF.nextObject();
                }

                //
                // Initialize the private key
                //
                InputStream secKeyInputStream = PGPUtil.getDecoderStream(new BufferedInputStream(new ByteArrayInputStream(pDecryptionKey.getBytes())));
                PGPPublicKeyEncryptedData pked = null;
                PGPPrivateKey pgpPrvKey = null;

                try {
                    PGPSecretKeyRingCollection pgpSecKeyRing = new PGPSecretKeyRingCollection(secKeyInputStream,new JcaKeyFingerprintCalculator());

                    Iterator it = enc.getEncryptedDataObjects();

                    while ((pgpPrvKey == null) && it.hasNext()) {
                        pked = (PGPPublicKeyEncryptedData) it.next();

                        PGPSecretKey pgpSecKey = pgpSecKeyRing.getSecretKey(pked.getKeyID());

                        if (pgpSecKey != null) {
                            pgpPrvKey = pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pDecryptionKeyPassword.toCharArray()));
                            //break;
                        }
                    }
                } finally {
                    secKeyInputStream.close();
                }

                //
                // If no private key found, we can't proceed
                //
                if (pgpPrvKey == null) {
                    result.getMessages().add("Private key for message not found.");
                    return result;
                }

                //
                // Initialize the decrypted data stream
                //
                InputStream decryptedDataStream = pked.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(pgpPrvKey));

                try {
                    //
                    // Initialize the compressed data stream
                    //
                    PGPObjectFactory decryptedObjectFactory = new PGPObjectFactory(decryptedDataStream, new JcaKeyFingerprintCalculator());
                    PGPCompressedData compressedData = (PGPCompressedData) decryptedObjectFactory.nextObject();
                    InputStream compressedDataStream = new BufferedInputStream(compressedData.getDataStream());

                    try {
                        PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(compressedDataStream, new JcaKeyFingerprintCalculator());
                        PGPOnePassSignature signature = null;
                        Object message = pgpObjectFactory.nextObject();

                        //
                        // Read through the objects in the file and act accordingly
                        //
                        while (message != null) {
                            if (message instanceof PGPOnePassSignatureList) {
                                result.setFileIsSigned(true);

                                //
                                // For the PGPOnePassSignatureList object, we initialize the public key to check the signature
                                // as well as initialize a PGPOnePassSignatureList object to build the local signature
                                // (this will be used to compare against the signature that is stored in the file)
                                //
                                InputStream pubKeyInputStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(pSignatureKey.getBytes()));

                                try {
                                    PGPPublicKeyRingCollection pgpPubKeyRing = new PGPPublicKeyRingCollection(pubKeyInputStream, new JcaKeyFingerprintCalculator());
                                    PGPOnePassSignatureList signatureList = (PGPOnePassSignatureList) message;

                                    signature = signatureList.get(0);

                                    PGPPublicKey pgpPubKey = pgpPubKeyRing.getPublicKey(signature.getKeyID());

                                    //
                                    // If public key found then initialize signature object, else just make a note and proceed
                                    //
                                    if (pgpPubKey != null) {
                                        signature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), pgpPubKey);
                                    } else {
                                        result.getMessages().add("Public key for message not found, signature cannot be verified.");
                                    }
                                } finally {
                                    pubKeyInputStream.close();
                                }
                            } else if (message instanceof PGPLiteralData) {
                                //
                                // PGPLiteralData is the encrypted portion of the file.  As we stream in the encrypted data
                                // it is decompressed and decrypted on-the-fly.  If present, our local signature object is also
                                // updated (this is the signature we're building in memory to compare to the stored signature)
                                //
                                PGPLiteralData literalData = (PGPLiteralData) message;
                                String outFileName = literalData.getFileName();

                                if ((outFileName == null) || outFileName.isEmpty()) {
                                    outFileName = pDefaultOutputFileName;
                                }

                                InputStream literalDataStream = literalData.getInputStream();
                                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pWorkingFolder + outFileName));

                                try {
                                    byte[] buf = new byte[BUFFER_SIZE];
                                    int len;

                                    while ((len = literalDataStream.read(buf, 0, buf.length)) > 0) {
                                        outputStream.write(buf, 0, len);

                                        if (signature != null) {
                                            signature.update(buf, 0, len);
                                        }
                                    }
                                } finally {
                                    outputStream.close();
                                }

                                result.setFileDecrypted(true);
                                result.setDecryptedFileName(outFileName);
                            } else if (message instanceof PGPSignatureList) {
                                //
                                // If we have a signature, verify the local reproduction matches the original
                                //
                                if (signature != null) {
                                    PGPSignatureList signatureList = (PGPSignatureList) message;

                                    if (signature.verify(signatureList.get(0))) {
                                        result.setFilePassedSignatureValidation(true);
                                    } else {
                                        result.setFilePassedSignatureValidation(false);
                                        result.getMessages().add("Signature verification failed (signature mismatch).");
                                    }
                                } else {
                                    result.setFilePassedSignatureValidation(false);
                                    result.getMessages().add("Signature verification failed (local signature is null).");
                                }
                            }
                            try {
                                message = pgpObjectFactory.nextObject();
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }

                        //
                        // After everything else is done, finally do the integrity check (if appropriate)
                        //
                        if (pked.isIntegrityProtected()) {
                            result.setFileIsIntegrityProtected(true);
                            result.setFilePassedIntegrityValidation(pked.verify());
                        }
                    } finally {
                        compressedDataStream.close();
                    }
                } finally {
                    decryptedDataStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (PGPException e) {
            System.err.println(e);

            if (e.getUnderlyingException() != null) {
                e.getUnderlyingException().printStackTrace();
            }
        } catch (Exception e) {
            System.err.println(e);
        }

        return result;
    }

    public static PgpFileDecryptionResult pgpDecryptUnsingedFile(String pWorkingFolder,
                                                                 String pInputFileName,
                                                                 String pDefaultOutputFileName,
                                                                 String pDecryptionKey,
                                                                 String pDecryptionKeyPassword) throws Exception {
        return pgpDecryptUnsingedFile(pWorkingFolder,pInputFileName,pDefaultOutputFileName,pDecryptionKey,pDecryptionKeyPassword,false);
    }

    public static PgpFileDecryptionResult pgpDecryptUnsingedFile(String pWorkingFolder,
                                                                 String pInputFileName,
                                                                 String pDefaultOutputFileName,
                                                                 String pDecryptionKey,
                                                                 String pDecryptionKeyPassword,
                                                                 boolean overrideOutputfileName) throws Exception {
        PgpFileDecryptionResult result = new PgpFileDecryptionResult();

        result.setEncryptedFileName(pWorkingFolder + pInputFileName);

        try {
            InputStream inputStream = PGPUtil.getDecoderStream(new BufferedInputStream(new FileInputStream(pWorkingFolder + pInputFileName)));

            try {
                PGPObjectFactory pgpF = new PGPObjectFactory(inputStream, new JcaKeyFingerprintCalculator());
                PGPEncryptedDataList enc;

                //
                // The first object might be a PGP marker packet, in which case just skip it.
                //
                Object o = pgpF.nextObject();

                if (o instanceof PGPEncryptedDataList) {
                    enc = (PGPEncryptedDataList) o;
                } else {
                    enc = (PGPEncryptedDataList) pgpF.nextObject();
                }

                //
                // Initialize the private key
                //
                InputStream secKeyInputStream = PGPUtil.getDecoderStream(new BufferedInputStream(new ByteArrayInputStream(pDecryptionKey.getBytes())));
                PGPPublicKeyEncryptedData pked = null;
                PGPPrivateKey pgpPrvKey = null;

                try {
                    PGPSecretKeyRingCollection pgpSecKeyRing = new PGPSecretKeyRingCollection(secKeyInputStream,new JcaKeyFingerprintCalculator());

                    Iterator it = enc.getEncryptedDataObjects();

                    while ((pgpPrvKey == null) && it.hasNext()) {
                        pked = (PGPPublicKeyEncryptedData) it.next();

                        PGPSecretKey pgpSecKey = pgpSecKeyRing.getSecretKey(pked.getKeyID());

                        if (pgpSecKey != null) {
                            pgpPrvKey = pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pDecryptionKeyPassword.toCharArray()));
                            //break;
                        }
                    }
                } finally {
                    secKeyInputStream.close();
                }

                //
                // If no private key found, we can't proceed
                //
                if (pgpPrvKey == null) {
                    result.getMessages().add("Private key for message not found.");
                    return result;
                }

                //
                // Initialize the decrypted data stream
                //
                InputStream decryptedDataStream = pked.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(pgpPrvKey));

                try {
                    //
                    // Initialize the compressed data stream
                    //
                    PGPObjectFactory decryptedObjectFactory = new PGPObjectFactory(decryptedDataStream,new JcaKeyFingerprintCalculator());
                    PGPCompressedData compressedData = (PGPCompressedData) decryptedObjectFactory.nextObject();
                    InputStream compressedDataStream = new BufferedInputStream(compressedData.getDataStream());

                    try {
                        PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(compressedDataStream,new JcaKeyFingerprintCalculator());
                        PGPOnePassSignature signature = null;
                        Object message = pgpObjectFactory.nextObject();

                        //
                        // Read through the objects in the file and act accordingly
                        //
                        while (message != null) {
                            if (message instanceof PGPLiteralData) {
                                //
                                // PGPLiteralData is the encrypted portion of the file.  As we stream in the encrypted data
                                // it is decompressed and decrypted on-the-fly.  Since there is no signature present we are not taking care of this scenario.
                                //
                                PGPLiteralData literalData = (PGPLiteralData) message;
                                String outFileName = literalData.getFileName();

                                if (overrideOutputfileName || ((outFileName == null) || outFileName.isEmpty())) {
                                    outFileName = pDefaultOutputFileName;
                                }

                                InputStream literalDataStream = literalData.getInputStream();
                                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pWorkingFolder + outFileName));

                                try {
                                    byte[] buf = new byte[BUFFER_SIZE];
                                    int len;

                                    while ((len = literalDataStream.read(buf, 0, buf.length)) > 0) {
                                        outputStream.write(buf, 0, len);

                                        if (signature != null) {
                                            signature.update(buf, 0, len);
                                        }
                                    }
                                } finally {
                                    outputStream.close();
                                }

                                result.setFileDecrypted(true);
                                result.setDecryptedFileName(outFileName);
                            }
                            try {
                                message = pgpObjectFactory.nextObject();
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }

                        //
                        // After everything else is done, finally do the integrity check (if appropriate)
                        //
                        if (pked.isIntegrityProtected()) {
                            result.setFileIsIntegrityProtected(true);
                            result.setFilePassedIntegrityValidation(pked.verify());
                        }
                    } finally {
                        compressedDataStream.close();
                    }
                } finally {
                    decryptedDataStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (PGPException e) {
            System.err.println(e);

            if (e.getUnderlyingException() != null) {
                e.getUnderlyingException().printStackTrace();
            }
        } catch (Exception e) {
            System.err.println(e);
        }

        return result;
    }

    private static enum Commands {
        Encrypt,
        Decrypt
    }


    private static enum FileOriginators {
        Intuit,
        Bank
    }
}
