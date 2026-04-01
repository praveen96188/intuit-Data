package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.paycycle.eft.FieldId;

@SuppressWarnings("unused")
public class Txp_TX_UI extends TxpRecordManager {
    public Txp_TX_UI() {
        super("/pay-def-tx-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));

        SpcfCalendar endDate = CalendarUtils.getLastDayOfQuarter(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, endDate.format("yyMMdd"));

        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 1));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
