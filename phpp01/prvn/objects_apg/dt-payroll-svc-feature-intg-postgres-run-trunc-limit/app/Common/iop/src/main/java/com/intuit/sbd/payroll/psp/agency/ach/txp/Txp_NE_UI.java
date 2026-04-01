package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

@SuppressWarnings("unused")
public class Txp_NE_UI extends TxpRecordManager {
    public Txp_NE_UI() {
        super("/pay-def-ne-ui.xml");
    }

    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getWithoutHyphens(getEin(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_YEAR, getEndDateYear(pEdr));

        // The tax quarter in this case is actually the last month of the tax quarter.
        String month = "" + CalendarUtils.getLastDayOfQuarter(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd()).getMonth();
        month = StringUtil.leftPad(month, "0", 2);
        getTxpTemplate().setFieldValue(FieldId.TAX_QUARTER, month);

        // First 15 digits of company name.  Upper case.  Space padded to a total of 30 chars.
        String erName = StringUtil.truncate(pEdr.getCompany().getLegalName().toUpperCase(), 15);
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, StringUtil.rightPad(erName, " ", 30));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
