package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Oklahoma withholding for TXP output
 */
@SuppressWarnings("unused")
public class Txp_OK_WH extends TxpRecordManager {
    public Txp_OK_WH() {
        super("/pay-def-ok.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getEin(pEdr); //  getStateOrFEIN(pEdr, 9);
        
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, "G*WTH*F*" + taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getFullEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, "RTNPYM");
        getTxpTemplate().setFieldValue(FieldId.CREATED_DATE, PSPDate.getPSPTime().format("yyyyMMdd"));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
