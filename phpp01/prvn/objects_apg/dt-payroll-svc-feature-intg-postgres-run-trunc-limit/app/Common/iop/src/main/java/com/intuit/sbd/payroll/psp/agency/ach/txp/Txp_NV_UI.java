package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

@SuppressWarnings("unused")
public class Txp_NV_UI extends TxpRecordManager {
    public Txp_NV_UI() {
        super("/pay-def-nv-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // "0" plus first 8 digits of state ein.
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, EntryDetailRecord.getFormattedAgencyIdForNV(getAgencyId(pEdr)));

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getWithoutHyphens(getEin(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
