package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_IA_UI extends TxpRecordManager {

    private static final String IA_TAX_TYPE_CODE = "13000";

    public Txp_IA_UI() {
        super("/pay-def-ia-ui.xml");
    }


    public void createTxpRecord(EntryDetailRecord pEdr) {

        // Get just the numbers.
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_IAWPN, getDigitsOnly(getAgencyId(pEdr)));

        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, IA_TAX_TYPE_CODE);
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getWithoutHyphens(getEin(pEdr)));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
