package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

/**
 * Massachusetts withholding for TXP output
 */
public class Txp_MA_WH extends TxpRecordManager {
    public Txp_MA_WH() {
        super("/pay-def-ma.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String ein = StringUtil.rightPad(getDigitsOnly(getEin(pEdr)), " ", 15);
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, ein);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        // Field 3 - Liability Date
        // If frequency = 'WEEKLY' or 'QUARTERMONTHLY' - date = last day of quarter
        DepositFrequencyCode frequency = pEdr.getMoneyMovementTransaction().getPaymentFrequency().getPaymentFrequencyId();
        if (frequency.equals(DepositFrequencyCode.WEEKLY) || frequency.equals(DepositFrequencyCode.QUARTERMONTHLY)) {
            SpcfCalendar endOfQuarter = CalendarUtils.getLastDayOfQuarter(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd());
            getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, endOfQuarter.format("yyMMdd"));
        } else {
            getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        }
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, pEdr.getCompany().getLegalName().toUpperCase());

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
