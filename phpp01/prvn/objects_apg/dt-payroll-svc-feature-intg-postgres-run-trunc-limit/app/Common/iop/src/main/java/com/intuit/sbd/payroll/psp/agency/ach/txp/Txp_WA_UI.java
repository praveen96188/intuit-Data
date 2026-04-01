package com.intuit.sbd.payroll.psp.agency.ach.txp;


import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eft.FieldId;

import java.math.BigDecimal;

public class Txp_WA_UI extends TxpRecordManager {

    public Txp_WA_UI() {
        super("/pay-def-wa-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        if(getAgencyId(pEdr).contains("-"))
        {
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_ID, "C"+getWithoutHyphens(getAgencyId(pEdr)));
        }
        else
        {
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_ID,"C" +getWithoutSpaces(getAgencyId(pEdr)));
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN,pEdr.getCompany().getSourceCompanyId());
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT,  (pEdr.getAmount().toString()));
        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}

