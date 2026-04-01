package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 18, 2010
 * Time: 7:16:50 AM
 */
public class PriorPaymentDTO {
    private DateDTO mPaymentDate;
    private DateDTO mPeriodEndDate;
    private SpcfMoney mTotalAmount;
    private boolean mIsVoid;
    private boolean mIsRefund;
    private Collection<TaxPaymentDTO> mTaxes;

    //convenience
    private String mSourceId;
    private String paymentTemplateCd;

    public DateDTO getPaymentDate() {
        return mPaymentDate;
    }

    public void setPaymentDate(DateDTO pPaymentDate) {
        mPaymentDate = pPaymentDate;
    }

    public DateDTO getPeriodEndDate() {
        return mPeriodEndDate;
    }

    public void setPeriodEndDate(DateDTO pPeriodEndDate) {
        mPeriodEndDate = pPeriodEndDate;
    }

    public SpcfMoney getTotalAmount() {
        return mTotalAmount;
    }

    public void setTotalAmount(SpcfMoney pTotalAmount) {
        mTotalAmount = pTotalAmount;
    }

    public boolean isIsVoid() {
        return mIsVoid;
    }

    public void setIsVoid(boolean pIsVoid) {
        mIsVoid = pIsVoid;
    }

    public String getSourceId() {
        return mSourceId;
    }

    public void setSourceId(String pSourceId) {
        mSourceId = pSourceId;
    }

    public Collection<TaxPaymentDTO> getTaxes() {
        return mTaxes;
    }

    public void setTaxes(Collection<TaxPaymentDTO> pTaxes) {
        mTaxes = pTaxes;
    }

    public boolean isRefund() {
        return mIsRefund;
    }

    public void setIsRefund(boolean mIsRefund) {
        this.mIsRefund = mIsRefund;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }
}
