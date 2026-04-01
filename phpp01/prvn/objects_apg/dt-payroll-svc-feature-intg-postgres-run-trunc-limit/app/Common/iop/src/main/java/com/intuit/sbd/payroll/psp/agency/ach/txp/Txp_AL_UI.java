package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.paycycle.eft.FieldId;

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_AL_UI extends TxpRecordManager {

    public Txp_AL_UI() {
        super("/pay-def-al-ui.xml");
    }

    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_QUARTER, CalendarUtils.getQuarterAsInt(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd()));
        getTxpTemplate().setFieldValue(FieldId.TAX_YEAR, getEndDateYear(pEdr));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
