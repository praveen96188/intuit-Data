package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

@SuppressWarnings("unused")
public class Txp_WI_UI extends TxpRecordManager {
    public Txp_WI_UI() {
        super("/pay-def-wi-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // 1st 6 digits of state tax ID + 12th digit of state tax ID.
        String ein =  getAgencyId(pEdr).substring(0,6) + getAgencyId(pEdr).substring(11, 12);
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, ein);

        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));

        // First 4 digits of company name with "The " removed if present.
        String erName = pEdr.getCompany().getLegalName().toUpperCase();
        if ( erName.startsWith("THE ")) {
            erName = erName.substring(4);
        }
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, StringUtil.truncate(erName, 6));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
