package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.AgencyIdRequirement;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplatePaymentMethod;
import com.paycycle.eft.FieldId;

/**
 * Maine withholding for TXP output
 */
public class Txp_ME_WH extends TxpRecordManager {
    public Txp_ME_WH() {
        super("/pay-def-me.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getDigitsOnly(getAgencyId(pEdr));
        if(taxId == null || taxId.length() != 11) {
            taxId = getEin(pEdr) + "00";
        } else {
            taxId = getAgencyId(pEdr).replaceAll("-", "").substring(0, 9);
            String ein = getEin(pEdr).replaceAll("-", "");
            if (taxId.equals(ein)) {
                taxId = getAgencyId(pEdr);
                //Only in this case stateId will be read and since it is stored with hyphen we will remove hyphen. In Other cases ein will be used which is stored without hyphen.
                taxId = getTaxIDForEPaymentTxpRecord(taxId);
            } else {
                taxId = getEin(pEdr) + "00";
            }
        }

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }

    /**
     * At present removing hyphen in taxId
     * @param taxId
     * @return
     */
    public static String  getTaxIDForEPaymentTxpRecord(String taxId){
        String regex= "-";
        String replacement ="";
        if(taxId !=null ) {
            return  taxId.replaceAll(regex,replacement).trim();
        }
        return null;
    }
}
