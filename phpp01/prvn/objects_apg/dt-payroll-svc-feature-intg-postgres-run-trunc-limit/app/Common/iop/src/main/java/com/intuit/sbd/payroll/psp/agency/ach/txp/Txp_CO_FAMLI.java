package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

public class Txp_CO_FAMLI extends TxpRecordManager{

    public Txp_CO_FAMLI() {
        super("/pay-def-co-pfml.xml");
    }
    @Override
    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN,  getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 4));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getEinWithHyphen(pEdr));
        getTxpTemplate().setFieldValue(FieldId.PAYOR_FEIN, getPayorFEIN());

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
