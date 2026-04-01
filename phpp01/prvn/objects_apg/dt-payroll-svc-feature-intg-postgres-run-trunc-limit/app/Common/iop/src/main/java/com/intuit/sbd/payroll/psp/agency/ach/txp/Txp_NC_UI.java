package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_NC_UI extends TxpRecordManager {
    public Txp_NC_UI() {
        super("/pay-def-nc-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // If EIN is only 7 digits, pad with a space.
        String ein = getDigitsOnly(getAgencyId(pEdr));
        if (ein.length() == 7)
            ein += " ";
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, ein);

        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));

        // If the amount is 0, override the tax amount type with a "Z".
        if (pEdr.getAmount().isZero()) {
            getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT_TYPE, "Z");
        }

        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
