package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

@SuppressWarnings("unused")
public class Txp_IL_UI extends TxpRecordManager {

    public Txp_IL_UI() {
        super("/pay-def-il-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // Fed. ID without hyphens.
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getWithoutHyphens(getEin(pEdr)));

        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
