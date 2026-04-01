package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;

/**
 * North Carolina withholding for TXP output
 */
public class Txp_NC_WH extends TxpRecordManager {
    public Txp_NC_WH() {
        super("/pay-def-nc.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getDigitsOnly(getAgencyId(pEdr));
        if(taxId.length() > 11){
            taxId = taxId.substring(0, 11);
        } else {
            taxId = StringUtil.rightPad(taxId, " ", 11);
        }

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
