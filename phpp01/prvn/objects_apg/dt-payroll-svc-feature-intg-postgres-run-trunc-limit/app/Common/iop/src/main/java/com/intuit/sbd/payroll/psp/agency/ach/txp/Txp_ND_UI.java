package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_ND_UI extends TxpRecordManager {
    public Txp_ND_UI() {
        super("/pay-def-nd-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // Remove non-numeric from state tax ID.
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));

        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getWithoutHyphens(getEin(pEdr)));

        // First 15 digits of company name.
        String erName = StringUtil.truncate(pEdr.getCompany().getLegalName().toUpperCase(), 15);
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, erName);

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
