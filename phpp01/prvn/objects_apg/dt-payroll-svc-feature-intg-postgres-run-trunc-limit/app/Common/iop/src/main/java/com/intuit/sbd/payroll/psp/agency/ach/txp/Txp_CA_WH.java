package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eft.FieldId;

/**
 * California withholding for TXP output
 */
public class Txp_CA_WH extends TxpRecordManager {
    private static String[] SIT = {"6"};
    private static String[] SDI = {"67"};

    public Txp_CA_WH() {
        super("/pay-def-ca.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        SpcfDecimal sitTax = getIndividualTaxAmount(pEdr, SIT);
        SpcfDecimal sdiTax = getIndividualTaxAmount(pEdr, SDI);

        if(sitTax.compareTo(SpcfMoney.ZERO) == -1){
            sdiTax = sdiTax.add(sitTax);
            sitTax = SpcfMoney.ZERO;
        }
        if(sdiTax.compareTo(SpcfMoney.ZERO) == -1){
            sitTax = sitTax.add(sdiTax);
            sdiTax = SpcfMoney.ZERO;
        }
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount((SpcfMoney) sdiTax, 8));
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, formatAmount((SpcfMoney) sitTax, 8));
        
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
