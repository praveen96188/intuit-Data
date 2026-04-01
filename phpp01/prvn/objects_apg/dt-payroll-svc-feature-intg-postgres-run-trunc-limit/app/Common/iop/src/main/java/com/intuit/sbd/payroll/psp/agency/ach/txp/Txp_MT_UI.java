package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

import java.math.BigDecimal;

public class Txp_MT_UI extends  TxpRecordManager {

    public Txp_MT_UI() {
        super("/pay-def-mt-ui.xml");
    }


    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_ID, getWithoutSpaces(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.DISCRETIONARY_DATA,pEdr.getCompany().getLegalName());
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE,pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, new BigDecimal(pEdr.getAmount().toString()));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
