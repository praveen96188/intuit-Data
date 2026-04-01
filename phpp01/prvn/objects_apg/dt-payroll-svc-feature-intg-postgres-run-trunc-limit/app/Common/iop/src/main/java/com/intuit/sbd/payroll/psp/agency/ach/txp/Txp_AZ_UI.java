package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.paycycle.eft.FieldId;

@SuppressWarnings("unused")
public class Txp_AZ_UI extends TxpRecordManager {

    public Txp_AZ_UI() {
        super("/pay-def-az-ui.xml");
    }


    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, getWithoutHyphens(getEin(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_YEAR, getEndDateYear(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_QUARTER, CalendarUtils.getQuarterAsInt(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd()));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
