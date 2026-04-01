package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Alabama withholding for TXP output
 */
public class Txp_AL_WH extends TxpRecordManager {
    public Txp_AL_WH() {
        super("/pay-def-al.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String idPrefix = "";
        if(getAgencyId(pEdr).length() == 6){
            idPrefix = "IW0000";
        } else if(getAgencyId(pEdr).length() == 10){
            idPrefix = "IW";
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, idPrefix + getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
