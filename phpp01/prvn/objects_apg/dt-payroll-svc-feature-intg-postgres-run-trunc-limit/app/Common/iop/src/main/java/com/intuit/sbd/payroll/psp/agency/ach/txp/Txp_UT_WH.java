package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Utah withholding for TXP output
 */
public class Txp_UT_WH extends TxpRecordManager {
    public Txp_UT_WH() {
        super("/pay-def-ut.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        int companyNameIndex = 0;
        if(pEdr.getCompany().getLegalName().toUpperCase().startsWith("THE ")){
            companyNameIndex = 4;
        } else if(pEdr.getCompany().getLegalName().toUpperCase().startsWith("THE")) {
            companyNameIndex = 3;
        }
        getTxpTemplate().setFieldValue(FieldId.FILING_NAME, pEdr.getCompany().getLegalName().substring(companyNameIndex).toUpperCase());

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
