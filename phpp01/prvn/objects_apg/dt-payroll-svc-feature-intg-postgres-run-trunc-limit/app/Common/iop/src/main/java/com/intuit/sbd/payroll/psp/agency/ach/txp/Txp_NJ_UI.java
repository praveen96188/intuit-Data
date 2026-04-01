package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.CompanyPaymentTemplateAgencyId;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_NJ_UI extends TxpRecordManager {
    public Txp_NJ_UI() {
        super("/pay-def-nj-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // The ein for New Jersey should be the fein (without hyphen) plus 3 trailing 0's.
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getWithoutHyphens(getEin(pEdr) + "000"));

        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));

        // First 4 digits of company name with "The " removed if present.
        String erName = pEdr.getCompany().getLegalName().toUpperCase();
        if ( erName.startsWith("THE ")) {
            erName = erName.substring(4);
        }
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, StringUtil.truncate(erName,4));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
