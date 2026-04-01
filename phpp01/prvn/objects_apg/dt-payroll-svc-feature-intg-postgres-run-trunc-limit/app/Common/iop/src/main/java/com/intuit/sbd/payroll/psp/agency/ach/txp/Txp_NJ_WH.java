package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.AgencyIdRequirement;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplatePaymentMethod;
import com.paycycle.eft.FieldId;

/**
 * New Jersey withholding for TXP output
 */
public class Txp_NJ_WH extends TxpRecordManager {
    public Txp_NJ_WH() {
        super("/pay-def-nj.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getDigitsOnly(getAgencyId(pEdr));

        if(taxId == null || taxId.length() != 12) {
            taxId = getEin(pEdr) + "000";
        } else {
            taxId = getAgencyId(pEdr).replaceAll("-", "").substring(0, 9);
            String ein = getEin(pEdr).replaceAll("-", "");
            if (taxId.equals(ein)) {
                taxId = getAgencyId(pEdr);
            } else {
                taxId = getEin(pEdr) + "000";
            }
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, "B" + getDigitsOnly(taxId));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
        int companyNameIndex = 0;
        if(pEdr.getCompany().getLegalName().toUpperCase().startsWith("THE ")){
            companyNameIndex = 4;
        } else if(pEdr.getCompany().getLegalName().toUpperCase().startsWith("THE")) {
            companyNameIndex = 3;
        }
        getTxpTemplate().setFieldValue(FieldId.NAME_CONTROL, pEdr.getCompany().getLegalName().substring(companyNameIndex).toUpperCase());

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
