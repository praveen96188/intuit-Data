package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Arkansas withholding for TXP output
 */
public class Txp_AR_WH extends TxpRecordManager {
    public Txp_AR_WH() {
        super("/pay-def-ar.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN2, getDigitsOnly(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());         
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        
        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
