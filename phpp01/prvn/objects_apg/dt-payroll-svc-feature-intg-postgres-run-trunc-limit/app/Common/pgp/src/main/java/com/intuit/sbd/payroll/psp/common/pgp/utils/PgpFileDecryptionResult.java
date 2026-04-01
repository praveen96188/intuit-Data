package com.intuit.sbd.payroll.psp.common.pgp.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kpaul
 * Date: 12/13/12
 * Time: 2:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class PgpFileDecryptionResult {
    private String mEncryptedFileName = null;
    private String mDecryptedFileName = null;
    private boolean mFileDecrypted = false;
    private boolean mFileIsSigned = false;
    private boolean mFileIsIntegrityProtected = false;
    private boolean mFilePassedSignatureValidation = false;
    private boolean mFilePassedIntegrityValidation = false;
    private List<String> mMessages = new ArrayList<String>();

    public String getEncryptedFileName() {
        return mEncryptedFileName;
    }

    public void setEncryptedFileName(final String pEncryptedFileName) {
        mEncryptedFileName = pEncryptedFileName;
    }

    public String getDecryptedFileName() {
        return mDecryptedFileName;
    }

    public void setDecryptedFileName(final String pDecryptedFileName) {
        mDecryptedFileName = pDecryptedFileName;
    }

    public boolean isFileDecrypted() {
        return mFileDecrypted;
    }

    public void setFileDecrypted(final boolean pFileDecrypted) {
        mFileDecrypted = pFileDecrypted;
    }

    public boolean isFileSigned() {
        return mFileIsSigned;
    }

    public void setFileIsSigned(final boolean pFileIsSigned) {
        mFileIsSigned = pFileIsSigned;
    }

    public boolean isFileIntegrityProtected() {
        return mFileIsIntegrityProtected;
    }

    public void setFileIsIntegrityProtected(final boolean pFileIsIntegrityProtected) {
        mFileIsIntegrityProtected = pFileIsIntegrityProtected;
    }

    public boolean isFilePassedSignatureValidation() {
        return mFilePassedSignatureValidation;
    }

    public void setFilePassedSignatureValidation(final boolean pFilePassedSignatureValidation) {
        mFilePassedSignatureValidation = pFilePassedSignatureValidation;
    }

    public boolean isFilePassedIntegrityValidation() {
        return mFilePassedIntegrityValidation;
    }

    public void setFilePassedIntegrityValidation(final boolean pFilePassedIntegrityValidation) {
        mFilePassedIntegrityValidation = pFilePassedIntegrityValidation;
    }

    public List<String> getMessages() {
        return mMessages;
    }
}
