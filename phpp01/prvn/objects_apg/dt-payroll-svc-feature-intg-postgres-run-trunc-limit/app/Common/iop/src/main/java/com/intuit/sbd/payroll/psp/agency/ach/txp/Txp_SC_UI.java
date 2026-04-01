package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

public class Txp_SC_UI extends TxpRecordManager {
    public Txp_SC_UI() {
        super("/pay-def-sc-ui.xml");
    }

    @Override
    public void createTxpRecord(EntryDetailRecord pEdr) {

        if(getAgencyId(pEdr).length()==8){
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_ID,getAgencyId(pEdr)+" ");
        }else if(getAgencyId(pEdr).length()==7){
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_ID,getAgencyId(pEdr)+"  ");
        }
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
