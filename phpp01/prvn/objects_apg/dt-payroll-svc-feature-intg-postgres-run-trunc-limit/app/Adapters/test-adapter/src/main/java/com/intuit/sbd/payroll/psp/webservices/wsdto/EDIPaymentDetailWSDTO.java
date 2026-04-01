package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 21, 2011
 * Time: 5:02:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class EDIPaymentDetailWSDTO {
    public int mTransactionSetId = 0;
    public String mTransactionId = null;
    public String mTransactionConfirmationNumber = null;
    public String mErrorCd = null;
    public String mErrorMessage = null;
    public Date mPaymentInitiationDate = null;
    public Date mPaymentSettlementDate = null;
    public String mTaxTypeCode = null;
    public String mFedTaxId = null;
    public Date mPaymentDueDate = null;
    public Date mPeriodEndDate = null;
    public Date mPeriodBeginDate = null;
    public Long mPaymentAmount = new Long("0");
    public String mStatusCd = com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus.None.toString();
    public Date mStatusEffectiveDate = null;
    public int mGroupId = 0;
    public String mGroupTxnTime = null;
    public Date mResponseDate = null;

}
