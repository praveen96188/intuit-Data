package com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2011
 * Time: 3:17:06 PM
 */
public class CheckDTO {

    private boolean isSuperCheck;

    // payer info
    private PayerDTO mPayerDTO;

    // payee info
    private PayeeDTO mPayeeDTO;

    // company information - not necessarily the payee
    private String mFEIN;
    private String mTaxId;
    private String mSourceCompanyNumber;
    private String mCompanyLegalName;

    // check info
    private BigDecimal mCheckAmount;
    private String mMemo;
    private Date mCheckDate;
    private Date mPrintDate;
    private String mCheckNumber;
    private String mRoutingNumber;
    private String mBankAccountNumber;

    private ArrayList<LineItemDTO> mLineItems;

    public PayerDTO getPayerDTO() {
        return mPayerDTO;
    }

    public void setPayerDTO(PayerDTO pPayerDTO) {
        mPayerDTO = pPayerDTO;
    }

    public PayeeDTO getPayeeDTO() {
        return mPayeeDTO;
    }

    public void setPayeeDTO(PayeeDTO pPayeeDTO) {
        mPayeeDTO = pPayeeDTO;
    }

    public String getFEIN() {
        if(mFEIN == null) {
            return "";
        }
        return mFEIN;
    }

    public void setFEIN(String pFEIN) {
        mFEIN = pFEIN;
    }

    public String getTaxId() {
        if(mTaxId == null) {
            return "";
        }
        return mTaxId;
    }

    public void setTaxId(String pTaxId) {
        mTaxId = pTaxId;
    }

    public String getSourceCompanyNumber() {
        return mSourceCompanyNumber;
    }

    public void setSourceCompanyNumber(String pSourceCompanyNumber) {
        mSourceCompanyNumber = pSourceCompanyNumber;
    }

    public String getCompanyLegalName() {
        return mCompanyLegalName;
    }

    public void setCompanyLegalName(String pCompanyLegalName) {
        mCompanyLegalName = pCompanyLegalName;
    }

    public BigDecimal getCheckAmount() {
        return mCheckAmount;
    }

    public void setCheckAmount(BigDecimal pCheckAmount) {
        mCheckAmount = pCheckAmount;
    }

    public String getMemo() {
        return mMemo;
    }

    public void setMemo(String pMemo) {
        mMemo = pMemo;
    }

    public Date getCheckDate() {
        return mCheckDate;
    }

    public void setCheckDate(Date pCheckDate) {
        mCheckDate = pCheckDate;
    }

    public Date getPrintDate() {
        return mPrintDate;
    }

    public void setPrintDate(Date pPrintDate) {
        mPrintDate = pPrintDate;
    }

    public String getCheckNumber() {
        return mCheckNumber;
    }

    public void setCheckNumber(String pCheckNumber) {
        mCheckNumber = pCheckNumber;
    }

    public String getRoutingNumber() {
        return mRoutingNumber;
    }

    public void setRoutingNumber(String pRoutingNumber) {
        mRoutingNumber = pRoutingNumber;
    }

    public String getBankAccountNumber() {
        return mBankAccountNumber;
    }

    public void setBankAccountNumber(String pBankAccountNumber) {
        mBankAccountNumber = pBankAccountNumber;
    }

    public ArrayList<LineItemDTO> getLineItems() {
        if (mLineItems == null) {
            mLineItems = new ArrayList<LineItemDTO>();
        }

        return mLineItems;
    }

    public void setLineItems(ArrayList<LineItemDTO> pLineItems) {
        mLineItems = pLineItems;
    }

    public boolean isSuperCheck() {
        return isSuperCheck;
    }

    public void setSuperCheck(boolean superCheck) {
        isSuperCheck = superCheck;
    }
}
