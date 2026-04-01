package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Created with IntelliJ IDEA.
 * User: cmehta1
 * Date: 9/5/17
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Txp_GA_UI extends TxpRecordManager {

    public Txp_GA_UI() {
        super("/pay-def-ga-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_ID,getWithoutHyphens(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_QUARTER,getQuarter(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_YEAR,getEndDateYear(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 9));

        //getTxpTemplate().setFieldValue(FieldId.SERVICE_PROVIDER,"CRI");
        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }


}
