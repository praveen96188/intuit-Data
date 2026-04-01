package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * User: ihannur
 * Date: 9/10/12
 * Time: 1:11 PM
 */

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_MS_WH extends TxpRecordManager {
    public Txp_MS_WH() {
        super("/pay-def-ms.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getFullEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
