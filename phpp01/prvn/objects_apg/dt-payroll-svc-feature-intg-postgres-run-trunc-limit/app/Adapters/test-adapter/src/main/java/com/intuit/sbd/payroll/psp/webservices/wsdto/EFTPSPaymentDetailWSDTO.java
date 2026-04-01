package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Feb 4, 2011
 * Time: 5:28:23 PM
 */
public class EFTPSPaymentDetailWSDTO {
    public int mTransactionSetId = 0;
    public int mTransactionId = 0;
    public String mReturnCd = com.intuit.sbd.payroll.psp.domain.ACHReturnReason.R01.toString();
    public Date mPaymentInitiationDate = null;
    public String mTaxTypeCode = null;
    public String mEftTransactionId = null;
    public String mAgencyPaymentId = null;
    public String mFedTaxId = null;
    public Date mPaymentDueDate = null;
    public Date mPeriodEndDate = null;
    public Long mPaymentAmount = new Long("0");
    public String mPaymentDetails = null;
    public String mStatusCd = com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus.None.toString();
    public Date mStatusEffectiveDate = null;
    public int mGroupId = 0;
    public String mRejectCd = null;
    public String mReason = null;
    public Date mResponseDate = null;
    public String mSameDayAckNumber = null;
}
