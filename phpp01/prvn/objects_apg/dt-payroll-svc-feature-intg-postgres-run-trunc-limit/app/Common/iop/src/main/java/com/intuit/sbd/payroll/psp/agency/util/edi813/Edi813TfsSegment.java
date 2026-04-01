package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

import java.util.Date;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:51:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813TfsSegment {
    private String mTaxTypeCode;
    private String mFedTaxId;
    private Date mTaxPeriodEndDate;
    private Edi813RefSegmentInner mRefSegment;
    private Stack<Edi813TiaSegmentInner> mTiaStack = new Stack<Edi813TiaSegmentInner>();

    public Edi813TfsSegment(RecordTemplate pTemplate) {
        mTaxTypeCode = pTemplate.getFieldValue(FieldId.EDI_813_SEG_TFS02);
        mFedTaxId = pTemplate.getFieldValue(FieldId.EDI_813_SEG_TFS06);
        mTaxPeriodEndDate = EftpsUtil.getDateFromShortDateString(pTemplate.getFieldValue(FieldId.EDI_813_SEG_TFS07));
    }

    public String getTaxTypeCode() {
        return mTaxTypeCode;
    }

    public void setTaxTypeCode(final String pTaxTypeCode) {
        mTaxTypeCode = pTaxTypeCode;
    }

    public String getFedTaxId() {
        return mFedTaxId;
    }

    public void setFedTaxId(final String pFedTaxId) {
        mFedTaxId = pFedTaxId;
    }

    public Date getTaxPeriodEndDate() {
        return mTaxPeriodEndDate;
    }

    public void setTaxPeriodEndDate(final Date pTaxPeriodEndDate) {
        mTaxPeriodEndDate = pTaxPeriodEndDate;
    }

    public Edi813RefSegmentInner getRefSegment() {
        return mRefSegment;
    }

    public void setRefSegment(final Edi813RefSegmentInner pRefSegment) {
        mRefSegment = pRefSegment;
    }

    public void setRefSegment(RecordTemplate pTemplate) {
        mRefSegment = new Edi813RefSegmentInner(pTemplate);
    }

    public Stack<Edi813TiaSegmentInner> getTiaStack() {
        return mTiaStack;
    }

    public void addTiaSegment(Edi813TiaSegmentInner pTiaSegment) {
        mTiaStack.push(pTiaSegment);
    }

    public void addTiaSegment(RecordTemplate pTemplate) {
        addTiaSegment(new Edi813TiaSegmentInner(pTemplate));
    }

    public Edi813TiaSegmentInner peekTiaSegment() {
        return mTiaStack.peek();
    }
}
