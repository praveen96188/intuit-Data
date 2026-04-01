package com.intuit.sbd.payroll.psp.agency.eftps;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 28, 2011
 * Time: 3:02:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdiResponseFileTxnDetails {
    private String mTxnId;
    private Integer mTxnSetId;
    private String mActionCode;
    private String mMessage;
    private String mErrorCd;

    public String getTxnId() {
        return mTxnId;
    }

    public void setTxnId(String mTxnId) {
        this.mTxnId = mTxnId;
    }

    public Integer getTxnSetId() {
        return mTxnSetId;
    }

    public void setTxnSetId(Integer mTxnSetId) {
        this.mTxnSetId = mTxnSetId;
    }

    public String getActionCode() {
        return mActionCode;
    }

    public void setActionCode(String mActionCode) {
        this.mActionCode = mActionCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String pMessage) {
        this.mMessage = pMessage;
    }

    public String getErrorCd() {
        return mErrorCd;
    }

    public void setErrorCd(String pErrorCd) {
        this.mErrorCd = pErrorCd;
    }
}
