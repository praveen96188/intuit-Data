package com.intuit.sbd.payroll.psp.common.pgp.impl;

import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class PGPWriterAESWithMultipleKeys extends PgpCommonEncryptedWriter{

    private int pgpEncryptedData;
    private static final int MAX_ITERATIONS = 10000;
    private List<String> mEncryptionKeyList;

    public PGPWriterAESWithMultipleKeys(List<String> encryptionKeyList, String signatureKey, String signatureKeyPassword) {
        super(encryptionKeyList, signatureKey, signatureKeyPassword);
    }

    public PGPWriterAESWithMultipleKeys(List<String> wcPgpKeys) {
        this(wcPgpKeys,PGPEncryptedData.AES_256);
        this.mEncryptionKeyList=wcPgpKeys;
    }

    public PGPWriterAESWithMultipleKeys(List<String> wcPgpKeys,int pgpEncryptedData) {
        super(wcPgpKeys);
        this.pgpEncryptedData=pgpEncryptedData;
        this.mEncryptionKeyList=wcPgpKeys;
    }

    @Override
    protected int getPGPEncryptedData() {
        return this.pgpEncryptedData;
    }

    @Override
    protected void keysEncryption(PGPEncryptedDataGenerator encryptedDataGenerator) throws Exception
    {
        for (String EncryptionKey: mEncryptionKeyList) {
            InputStream keyIn = new ByteArrayInputStream(EncryptionKey.getBytes());
            List<PGPPublicKey> encKey = PgpFileUtils.readMultiplePublicKeys(keyIn,MAX_ITERATIONS);
            for(PGPPublicKey key:encKey) {
                encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(key).setProvider("BC"));
            }
        }
    }
}
