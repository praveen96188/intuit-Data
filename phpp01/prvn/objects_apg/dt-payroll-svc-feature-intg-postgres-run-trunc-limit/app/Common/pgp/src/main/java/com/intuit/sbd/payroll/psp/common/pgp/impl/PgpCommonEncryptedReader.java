package com.intuit.sbd.payroll.psp.common.pgp.impl;

import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileSourceCode;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;

import java.io.*;
import java.security.Security;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/11/13
 * Time: 11:30 AM
 */
public class PgpCommonEncryptedReader implements PgpReader {

    private static final int BUFFER_SIZE = 1 << 16;

    private static Boolean mSkipSignatureVerification;
    private File mEncryptedFile;
    private String mDecryptionKey;
    private String mDecryptionKeyPassword;
    private String mSignatureKey;
    private PgpFileSourceCode mPgpFileSourceCode = PgpFileSourceCode.Bank;

    private ByteArrayOutputStream mByteArrayOutputStream;
    private StringReader mStringReader;
    private BufferedReader mBufferedReader;

    private static final SpcfLogger logger = SpcfLogManager.getLogger(PgpCommonEncryptedReader.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void open(String pFileName) throws Exception {
        mEncryptedFile = new File(pFileName);

        try {
            initialize();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize PgpEncryptedReader object", e);
        }
    }

    public void open(String pFileName, PgpFileSourceCode pPgpFileSourceCode) throws Exception {
        mEncryptedFile = new File(pFileName);
        mPgpFileSourceCode = pPgpFileSourceCode;

        try {
            initialize();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize PgpEncryptedReader object", e);
        }
    }

    public void open(File pFile) throws Exception {
        mEncryptedFile = pFile;

        try {
            initialize();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize PgpEncryptedReader object", e);
        }
    }

    public PgpCommonEncryptedReader(String decryptionKey, String decryptionKeyPassword) {

        //Intuits private key and Password to decrypt the file
        this.mDecryptionKey = decryptionKey;
        this.mDecryptionKeyPassword = decryptionKeyPassword;
        this.mSkipSignatureVerification = false;
    }

    public PgpCommonEncryptedReader(String decryptionKey, String decryptionKeyPassword,String signatureKey) {

        //Intuits private key and Password to decrypt the file
        this.mDecryptionKey = decryptionKey;
        this.mDecryptionKeyPassword = decryptionKeyPassword;

        if(!StringUtils.isEmpty(signatureKey)) {
            throw new RuntimeException("Unable to initialize PgpEncryptedReader object - Signature Key is empty or null");
        } else {
            this.mSkipSignatureVerification = true;
            this.mSignatureKey = signatureKey;
        }
    }

    private void initialize() throws Exception {
        try {
            logger.info(String.format("Decrypting file: %s", mEncryptedFile.getName()));
            InputStream inputStream = PGPUtil.getDecoderStream(new BufferedInputStream(new FileInputStream(mEncryptedFile)));

            try {
                PGPObjectFactory pgpF = new PGPObjectFactory(inputStream,new JcaKeyFingerprintCalculator());
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
                InputStream secKeyInputStream = PGPUtil.getDecoderStream(new BufferedInputStream(new ByteArrayInputStream(mDecryptionKey.getBytes())));
                PGPPublicKeyEncryptedData pked = null;
                PGPPrivateKey pgpPrvKey = null;

                try {
                    PGPSecretKeyRingCollection pgpSecKeyRing = new PGPSecretKeyRingCollection(secKeyInputStream, new JcaKeyFingerprintCalculator());

                    Iterator it = enc.getEncryptedDataObjects();

                    while ((pgpPrvKey == null) && it.hasNext()) {
                        pked = (PGPPublicKeyEncryptedData) it.next();

                        PGPSecretKey pgpSecKey = pgpSecKeyRing.getSecretKey(pked.getKeyID());

                        if (pgpSecKey != null) {
                            pgpPrvKey = pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(mDecryptionKeyPassword.toCharArray()));
                        }
                    }
                } catch (Exception e){
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    secKeyInputStream.close();
                }

                //
                // If no private key found, we can't proceed
                //
                if (pgpPrvKey == null) {
                    throw new RuntimeException("Private key for message not found.");
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
                                logger.info(String.format("File is signed: %s", true));

                                //
                                // For the PGPOnePassSignatureList object, we initialize the public key to check the signature
                                // as well as initialize a PGPOnePassSignatureList object to build the local signature
                                // (this will be used to compare against the signature that is stored in the file)
                                //
                                InputStream pubKeyInputStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(mSignatureKey.getBytes()));

                                try {
                                    PGPPublicKeyRingCollection pgpPubKeyRing = new PGPPublicKeyRingCollection(pubKeyInputStream, new JcaKeyFingerprintCalculator());
                                    PGPOnePassSignatureList signatureList = (PGPOnePassSignatureList) message;

                                    if (!mSkipSignatureVerification) {
                                        signature = signatureList.get(0);
                                        PGPPublicKey pgpPubKey = pgpPubKeyRing.getPublicKey(signature.getKeyID());

                                        //
                                        // If public key found then initialize signature object, else just make a note and proceed
                                        //
                                        if (pgpPubKey != null) {
                                            signature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), pgpPubKey);
                                        } else {
                                            throw new RuntimeException("Public key for message not found, signature cannot be verified.");
                                        }
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

                                InputStream literalDataStream = literalData.getInputStream();
                                mByteArrayOutputStream = new ByteArrayOutputStream();

                                byte[] buf = new byte[BUFFER_SIZE];
                                int len;

                                while ((len = literalDataStream.read(buf, 0, buf.length)) > 0) {
                                    mByteArrayOutputStream.write(buf, 0, len);

                                    if (signature != null) {
                                        signature.update(buf, 0, len);
                                    }
                                }

                                logger.info(String.format("File decrypted: %s", true));
                            } else if (message instanceof PGPSignatureList) {
                                //
                                // If we have a signature, verify the local reproduction matches the original
                                //
                                if (signature != null) {
                                    PGPSignatureList signatureList = (PGPSignatureList) message;

                                    if (signature.verify(signatureList.get(0))) {
                                        logger.info(String.format("File passed signature validation: %s", true));
                                    } else {
                                        if (mSkipSignatureVerification) {
                                            logger.error("Signature verification failed (signature mismatch).");
                                        } else {
                                            throw new RuntimeException("Signature verification failed (signature mismatch).");
                                        }
                                    }
                                } else {
                                    if (mSkipSignatureVerification) {
                                        logger.error("Signature verification failed (local signature is null).");
                                    } else {
                                        throw new RuntimeException("Signature verification failed (local signature is null).");
                                    }
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
                        logger.info(String.format("File is integrity protected: %s", pked.isIntegrityProtected()));
                        if (pked.isIntegrityProtected()) {
                            if (pked.verify()) {
                                logger.info(String.format("File passed integrity validation: %s", true));
                            } else {
                                throw new RuntimeException(String.format("File %s failed integrity validation.", mEncryptedFile.getName()));
                            }
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

            mStringReader = new StringReader(mByteArrayOutputStream.toString());
            mBufferedReader = new BufferedReader(mStringReader);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean ready() throws IOException {
        return mBufferedReader.ready();
    }

    public String readLine() throws IOException {
        return mBufferedReader.readLine();
    }

    public void close() throws IOException {
        if (mBufferedReader != null)
            mBufferedReader.close();
        if (mStringReader != null)
            mStringReader.close();
    }
}