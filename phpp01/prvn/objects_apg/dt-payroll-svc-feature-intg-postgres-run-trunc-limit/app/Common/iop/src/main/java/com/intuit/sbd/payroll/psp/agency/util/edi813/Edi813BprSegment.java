package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:51:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813BprSegment {
    private String mTransHandlingCode;
    private BigDecimal mSegmentTaxAmount;
    private String mCreditDebitFlag;
    private String mPaymentMethodCode;
    private String mDfiIdNum;
    private String mAccountNumberQual;
    private String mAccountNumber;
    private Date mSettlementDate;

    public Edi813BprSegment(RecordTemplate pTemplate) {
        mTransHandlingCode = pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR01);
        mSegmentTaxAmount = new BigDecimal(pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR02)).scaleByPowerOfTen(-2);
        mCreditDebitFlag = pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR03);
        mPaymentMethodCode = pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR04);
        mDfiIdNum = pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR13);
        mAccountNumberQual = pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR14);
        mAccountNumber = pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR15);
        mSettlementDate = EftpsUtil.getDateFromShortDateString(pTemplate.getFieldValue(FieldId.EDI_813_SEG_BPR16));
    }

    public String getTransHandlingCode() {
        return mTransHandlingCode;
    }

    public void setTransHandlingCode(final String pTransHandlingCode) {
        mTransHandlingCode = pTransHandlingCode;
    }

    public BigDecimal getSegmentTaxAmount() {
        return mSegmentTaxAmount;
    }

    public void setSegmentTaxAmount(final BigDecimal pSegmentTaxAmount) {
        mSegmentTaxAmount = pSegmentTaxAmount;
    }

    public String getCreditDebitFlag() {
        return mCreditDebitFlag;
    }

    public void setCreditDebitFlag(final String pCreditDebitFlag) {
        mCreditDebitFlag = pCreditDebitFlag;
    }

    public String getPaymentMethodCode() {
        return mPaymentMethodCode;
    }

    public void setPaymentMethodCode(final String pPaymentMethodCode) {
        mPaymentMethodCode = pPaymentMethodCode;
    }

    public String getDfiIdNum() {
        return mDfiIdNum;
    }

    public void setDfiIdNum(final String pDfiIdNum) {
        mDfiIdNum = pDfiIdNum;
    }

    public String getAccountNumberQual() {
        return mAccountNumberQual;
    }

    public void setAccountNumberQual(final String pAccountNumberQual) {
        mAccountNumberQual = pAccountNumberQual;
    }

    public String getAccountNumber() {
        return mAccountNumber;
    }

    public void setAccountNumber(final String pAccountNumber) {
        mAccountNumber = pAccountNumber;
    }

    public Date getSettlementDate() {
        return mSettlementDate;
    }

    public void setSettlementDate(final Date pSettlementDate) {
        mSettlementDate = pSettlementDate;
    }
}
