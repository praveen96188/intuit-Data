package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Hawaii withholding for TXP output
 */
public class Txp_HI_WH extends TxpRecordManager {
    public Txp_HI_WH() {
        super("/pay-def-hi.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getAgencyId(pEdr);
        String hawaiiTaxId;
        String hawaiiTaxIdSuffix;
        if(taxId.startsWith("1") || taxId.startsWith("2") || taxId.startsWith("3") || taxId.startsWith("4")){
            taxId = taxId.substring(0, 8);
            hawaiiTaxId = "000000000000";
            hawaiiTaxIdSuffix = "00";
        } else {
            if(taxId.startsWith("WH")){
                hawaiiTaxId=taxId.substring(0, 15).replace("-", "");
            }
            else {
                hawaiiTaxId = taxId.substring(0, 9)+"   ";
            }
            hawaiiTaxIdSuffix = taxId.substring(taxId.length()-2, taxId.length());
            taxId = "00000000";
        }
        getTxpTemplate().setFieldValue(FieldId.GENERAL_EXCISE_NUMBER, taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_START_DATE, getBeginDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.HAWAII_TAX_ID, hawaiiTaxId);
        getTxpTemplate().setFieldValue(FieldId.HAWAII_TAX_ID_SUFFIX, hawaiiTaxIdSuffix);

        String annualPeriodIndicator = "N";
        if(pEdr.getMoneyMovementTransaction().getPaymentFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.ANNUAL)){
            annualPeriodIndicator = "Y";
        }        
        getTxpTemplate().setFieldValue(FieldId.ANNUAL_PERIOD_INDICATOR, annualPeriodIndicator);

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
