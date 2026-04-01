package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

/**
 * Missouri UI for TXP output
 */
public class Txp_MO_UI extends TxpRecordManager {
    public Txp_MO_UI() {
        super("/pay-def-mo-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        String ein = StringUtil.rightPad(getWithoutHyphens(getAgencyId(pEdr)), "0", 15);
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, ein);
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getDigitsOnly(getEin(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, StringUtil.truncate(pEdr.getCompany().getLegalName().toUpperCase(), 24));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}