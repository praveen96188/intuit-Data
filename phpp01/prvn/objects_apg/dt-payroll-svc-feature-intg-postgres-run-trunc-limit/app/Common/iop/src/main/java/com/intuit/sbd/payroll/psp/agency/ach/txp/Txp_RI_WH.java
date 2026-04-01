package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.AgencyIdRequirement;
import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplatePaymentMethod;
import com.paycycle.eft.FieldId;

/**
 * Rhode Island withholding for TXP output
 */
public class Txp_RI_WH extends TxpRecordManager {
    public Txp_RI_WH() {
        super("/pay-def-ri.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getStateOrFEIN(pEdr, 9);

        if(!taxId.endsWith("00")) {
            taxId += "00";            
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        
        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
