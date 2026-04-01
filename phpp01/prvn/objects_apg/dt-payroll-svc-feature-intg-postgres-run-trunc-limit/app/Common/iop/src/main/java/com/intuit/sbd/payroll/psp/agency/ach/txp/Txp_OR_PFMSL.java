package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

/**
 * OR ACH Credit
 */

// Instantiated by class name stored in payment template.
public class Txp_OR_PFMSL extends TxpRecordManager {
    public Txp_OR_PFMSL() {
        super("/pay-def-or-pfmsl.xml");
    }

    public void createTxpRecord(EntryDetailRecord pEdr) {

        String ein = StringUtil.leftPad(getWithoutHyphens(getAgencyId(pEdr)), "0", 9);
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, ein);
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));
        pEdr.setTxpRecordData(getTxpTemplate().toString());

    }
}