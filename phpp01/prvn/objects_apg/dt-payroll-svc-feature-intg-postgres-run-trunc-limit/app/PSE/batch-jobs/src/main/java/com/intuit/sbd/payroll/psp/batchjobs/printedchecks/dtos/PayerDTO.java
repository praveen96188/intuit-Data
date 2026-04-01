package com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2011
 * Time: 3:27:16 PM
 */
public class PayerDTO extends PayeeDTO {
    private byte[] mLogo;
    private byte[] mBankLogo;
    private byte[] mSignature;    

    public byte[] getLogo() {
        return mLogo;
    }

    public void setLogo(byte[] pLogo) {
        mLogo = pLogo;
    }

    public byte[] getBankLogo() {
        return mBankLogo;
    }

    public void setBankLogo(byte[] pBankLogo) {
        mBankLogo = pBankLogo;
    }

    public byte[] getSignature() {
        return mSignature;
    }

    public void setSignature(byte[] pSignature) {
        mSignature = pSignature;
    }
}
