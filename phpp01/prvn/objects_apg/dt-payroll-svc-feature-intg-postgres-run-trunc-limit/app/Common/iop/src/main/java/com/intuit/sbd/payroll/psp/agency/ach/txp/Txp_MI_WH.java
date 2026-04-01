package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateFrequency;
import com.paycycle.eft.FieldId;

/**
 * Michigan withholding for TXP output
 */
public class Txp_MI_WH extends TxpRecordManager {
    public Txp_MI_WH() {
        super("/pay-def-mi.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getAgencyId(pEdr);

        if(taxId != null && taxId.charAt(2) != '-') {
            taxId = new StringBuffer(taxId).insert(2, "-").toString(); 
        }

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_PIN, taxId);

        PaymentTemplateFrequency paymentFrequency = pEdr.getMoneyMovementTransaction().getPaymentFrequency();

        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, paymentFrequency.getTaxCodeId());

        if (DepositFrequencyCode.SEMIWEEKLY.equals(paymentFrequency.getPaymentFrequencyId()) || DepositFrequencyCode.NEXTBANKINGDAY.equals(paymentFrequency.getPaymentFrequencyId()) ) {
            getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getMonthEndDate(pEdr));
        } else {
            getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        }

        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
