package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.*;
import com.paycycle.eft.FieldId;

/**
 * Iowa withholding for TXP output
 */
public class Txp_IA_WH extends TxpRecordManager {
    public Txp_IA_WH() {
        super("/pay-def-ia.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getDigitsOnly(getAgencyId(pEdr));

        if(taxId == null || (taxId.length() != 12 && taxId.length() != 9)) {
            taxId = getEin(pEdr) + "001";
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_IAWPN, "000" + getDigitsOnly(taxId));
        } else if (taxId.length() == 12){
            taxId = getAgencyId(pEdr).replaceAll("-", "");
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_IAWPN, "000" + getDigitsOnly(taxId));
        } else {
            taxId = getAgencyId(pEdr).replaceAll("-", "");
            getTxpTemplate().setFieldValue(FieldId.TAXPAYER_IAWPN, "000000" + getDigitsOnly(taxId));
        }

        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
