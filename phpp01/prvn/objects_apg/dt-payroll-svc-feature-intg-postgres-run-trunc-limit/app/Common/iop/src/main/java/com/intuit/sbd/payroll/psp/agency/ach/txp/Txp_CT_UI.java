package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

public class Txp_CT_UI extends TxpRecordManager{

    public Txp_CT_UI() {
        super("/pay-def-ct-ui.xml");
    }
    @Override
    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getWithoutHyphens(getEin(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
