package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;


// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_MD_UI extends TxpRecordManager {

    public Txp_MD_UI() {
        super("/pay-def-md-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // State tax ID starting with "00".
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
