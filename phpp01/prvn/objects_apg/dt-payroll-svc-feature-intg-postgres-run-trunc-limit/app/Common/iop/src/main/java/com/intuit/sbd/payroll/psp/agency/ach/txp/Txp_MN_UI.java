package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_MN_UI extends TxpRecordManager {

    public Txp_MN_UI() {
        super("/pay-def-mn-ui.xml");
    }

    public void createTxpRecord(EntryDetailRecord pEdr) {

        // If the 9th digit is a ‘-‘ or if the AgencyId is 8-digit long:
        //     Starting to the left of the ‘-‘, shift the digits to the right and fill in zeros on the left
        // Otherwise:
        //    Shift the digits to the right 2 places starting with the 7th digit and fill in zeros on the left
        String ein = getAgencyId(pEdr);
        if((ein.length() > 8 && ein.charAt(8) == '-') || ein.length() == 8) {
            ein = "0" + ein.substring(0,8);
        } else {
            ein = "00" + ein.substring(0,7);
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, ein);

        // If the FEIN is blank, use '999999999'.
        String fein = getEin(pEdr);
        if (fein == null || fein.isEmpty()) {
            fein = "999999999";
        } else {
            fein = getWithoutHyphens(fein);
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN, fein);

        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
