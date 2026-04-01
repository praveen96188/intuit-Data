package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

import java.util.Random;

/**
 * User: ihannur
 * Date: 8/19/13
 * Time: 3:42 PM
 */
@SuppressWarnings("unused")
public class Txp_PA_UI extends TxpRecordManager {

    public Txp_PA_UI() {
        super("/pay-def-pa-ui.xml");
    }

    public void createTxpRecord(EntryDetailRecord pEdr) {
        Random random = new Random();
        long transactionId = random.nextLong();
        if (transactionId < 0) {
            transactionId = transactionId * -1;
        }

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, String.format("%-15s", getWithoutHyphens(getEin(pEdr))));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        getTxpTemplate().setFieldValue(FieldId.TRANSACTION_ID, String.valueOf(transactionId));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
