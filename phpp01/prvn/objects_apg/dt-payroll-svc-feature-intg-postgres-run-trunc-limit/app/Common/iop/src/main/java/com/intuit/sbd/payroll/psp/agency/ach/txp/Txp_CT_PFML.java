package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

public class Txp_CT_PFML extends TxpRecordManager{

    public Txp_CT_PFML() {
        super("/pay-def-ct-pfml.xml");
    }
    @Override
    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().format("yyyy-MM-dd"));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
