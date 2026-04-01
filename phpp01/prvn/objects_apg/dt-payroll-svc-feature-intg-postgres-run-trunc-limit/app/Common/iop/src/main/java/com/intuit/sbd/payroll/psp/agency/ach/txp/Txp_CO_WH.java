package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.CompanyPaymentTemplateAgencyId;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Colorado withholding for TXP output
 */
public class Txp_CO_WH extends TxpRecordManager {
    public Txp_CO_WH() {
        super("/pay-def-co.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        DomainEntitySet<CompanyPaymentTemplateAgencyId> companyPaymentTemplateAgencyIds = pEdr.getMoneyMovementTransaction().getCompanyPaymentMethod().getCompanyAgencyPaymentTemplate()
                .getCompanyPaymentTemplateAgencyIdCollection().find(CompanyPaymentTemplateAgencyId.Name().equalTo("State EFT Number"));
        if (companyPaymentTemplateAgencyIds.size() > 0) {
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_PIN, companyPaymentTemplateAgencyIds.get(0).getAgencyTaxpayerId());
            getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
            getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
            getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

            pEdr.setTxpRecordData(getTxpTemplate().toString());
        }
    }
}
