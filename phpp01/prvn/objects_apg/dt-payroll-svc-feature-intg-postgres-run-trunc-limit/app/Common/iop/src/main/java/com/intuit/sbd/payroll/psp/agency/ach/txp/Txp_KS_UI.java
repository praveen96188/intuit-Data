package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_KS_UI extends TxpRecordManager {

    public Txp_KS_UI() {
        super("/pay-def-ks-ui.xml");
    }


    public void createTxpRecord(EntryDetailRecord pEdr) {

        // Take up to the first 6 digits of the agency ID, then pad the right side of the EIN with 0's up to 15 digits.
        String ein = StringUtil.rightPad(StringUtil.truncate(getAgencyId(pEdr), 6), "0", 15);
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, ein);

        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
