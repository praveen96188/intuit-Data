package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eft.FieldId;

/**
 * Oregon withholding for TXP output
 */
public class Txp_OR_WH extends TxpRecordManager {
    public Txp_OR_WH() {
        super("/pay-def-or.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getQuarterEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(new SpcfMoney("0.00"), 8));
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, formatAmount(pEdr.getAmount(), 8));
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2, formatAmount(new SpcfMoney("0.00"), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
