package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;


@SuppressWarnings( "unused" )
public class Txp_FL_UI extends TxpRecordManager {

    private static final String FL_STATE_TAX_ID_PREFIX = "00000000";
    private static final String FL_TAX_TYPE_CODE = "05425";
    private static final int FL_MAX_EIN_LENGTH = 7;

    public Txp_FL_UI() {
        super("/pay-def-fl-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        // Prefix plus up to first 7 numeric digits from State Tax ID.
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, FL_STATE_TAX_ID_PREFIX + getDigitsOnly(getAgencyId(pEdr)));

        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, FL_TAX_TYPE_CODE);
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));

        // TODO: AS400 example shows a "1" in this field, but XML file sets a value of "T".
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT_TYPE, "1");

        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
