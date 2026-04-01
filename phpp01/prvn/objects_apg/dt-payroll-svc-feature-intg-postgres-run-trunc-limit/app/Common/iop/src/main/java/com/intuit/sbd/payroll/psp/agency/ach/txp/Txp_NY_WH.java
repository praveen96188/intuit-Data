package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.CompanyPaymentTemplateAgencyId;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eft.FieldId;

/**
 * New York withholding for TXP output
 */
public class Txp_NY_WH extends TxpRecordManager {
    private static String[] SIT = {"36"};
    private static String[] CITY = {"54"};
    private static String[] YONKERS = {"56", "57"};

    public Txp_NY_WH() {
        super("/pay-def-ny.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        DomainEntitySet<CompanyPaymentTemplateAgencyId> companyPaymentTemplateAgencyIds = pEdr.getMoneyMovementTransaction().getCompanyPaymentMethod().getCompanyAgencyPaymentTemplate()
                .getCompanyPaymentTemplateAgencyIdCollection().find(CompanyPaymentTemplateAgencyId.Name().equalTo("State Access Code"));


        if(companyPaymentTemplateAgencyIds.size() > 0){
            String taxId = getDigitsOnly(getAgencyId(pEdr));

            if(taxId.length() > 9){
                taxId = taxId.substring(0, 9);
            }
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, taxId);
            getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
            getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
            SpcfMoney sitTax = getIndividualTaxAmount(pEdr, SIT);
            SpcfMoney cityTax = getIndividualTaxAmount(pEdr, CITY);
            SpcfMoney yonkersTax = getIndividualTaxAmount(pEdr, YONKERS);
            String accessToken = companyPaymentTemplateAgencyIds.get(0).getAgencyTaxpayerId();

            if(sitTax.compareTo(SpcfMoney.ZERO) == 1){
                getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT_TYPE, "S");
                getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(sitTax, 8));
            } else {
                getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, "ZERO");
            }
            if(cityTax.compareTo(SpcfMoney.ZERO) == 1){
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1_TYPE, "C");
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, formatAmount(cityTax, 8));
            } else {
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, "ZERO");
            }
            if(yonkersTax.compareTo(SpcfMoney.ZERO) == 1){
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2_TYPE, "L");
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2, formatAmount(yonkersTax, 8));
            } else {
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2, "ZERO");
            }
            if(accessToken != null && !accessToken.isEmpty()){
                getTxpTemplate().setFieldValue(FieldId.TAXPAYER_PIN, accessToken);
            } else {
                getTxpTemplate().setFieldValue(FieldId.TAXPAYER_PIN, "999999");
            }
            pEdr.setTxpRecordData(getTxpTemplate().toString());
        }        
    }
}
