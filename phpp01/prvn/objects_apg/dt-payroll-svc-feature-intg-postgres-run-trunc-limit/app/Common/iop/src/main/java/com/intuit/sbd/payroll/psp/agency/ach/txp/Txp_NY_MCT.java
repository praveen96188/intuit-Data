package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.CompanyPaymentTemplateAgencyId;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: May 23, 2011
 * Time: 12:21:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class Txp_NY_MCT extends TxpRecordManager {
    public Txp_NY_MCT() {
        super("/pay-def-ny-mct.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {

        DomainEntitySet<CompanyPaymentTemplateAgencyId> companyPaymentTemplateAgencyIds = pEdr.getMoneyMovementTransaction().getCompanyPaymentMethod().getCompanyAgencyPaymentTemplate()
                .getCompanyPaymentTemplateAgencyIdCollection().find(CompanyPaymentTemplateAgencyId.Name().equalTo("State Access Code"));

        if (companyPaymentTemplateAgencyIds.size() > 0) {
            String taxId = getDigitsOnly(getAgencyId(pEdr));
            if (taxId.length() > 9) {
                taxId = taxId.substring(0, 9);
            }
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, taxId);
            getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, "MT");
            getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
            getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1_TYPE, "M");
            getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, "0000000000");
            getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT_TYPE, "T");
            getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_PIN, companyPaymentTemplateAgencyIds.get(0).getAgencyTaxpayerId());

            pEdr.setTxpRecordData(getTxpTemplate().toString());
        }
    }
}
