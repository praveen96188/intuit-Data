package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.paycycle.eft.FieldId;

/**
 * Ohio withholding for TXP output
 */
public class Txp_OH_WH extends TxpRecordManager {
    public Txp_OH_WH() {
        super("/pay-def-oh.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getDigitsOnly(getAgencyId(pEdr));
        if(taxId.length() == 8){
            int checkDigit = SpcfUtils.getCheckDigit(taxId);
            taxId += String.valueOf(checkDigit);
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
