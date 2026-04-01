package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

@SuppressWarnings("unused")
public class Txp_TN_UI extends TxpRecordManager {
    public Txp_TN_UI() {
        super("/pay-def-tn-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getWithoutHyphens(getEin(pEdr)));

        // First 6 digits of company name.
        String erName = StringUtil.truncate(pEdr.getCompany().getLegalName().toUpperCase(), 6);
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, erName);

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
