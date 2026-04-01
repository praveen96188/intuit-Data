package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: May 23, 2011
 * Time: 12:33:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class Txp_ID_UI extends TxpRecordManager {
    public Txp_ID_UI() {
        super("/pay-def-id.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
