package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;

/**
 * Illinois withholding for TXP output
 */
public class Txp_IL_WH extends TxpRecordManager {
    public Txp_IL_WH() {
        super("/pay-def-il.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        String taxId = getDigitsOnly(getAgencyId(pEdr));
        if (taxId.length() == 12) {
            int checkDigit = getCheckDigit(taxId);
            taxId += String.valueOf(checkDigit);
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getQuarterEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }

    private int getCheckDigit(String pNumber) {
        if (pNumber == null || pNumber.trim().equals("")) {
            throw new RuntimeException("Error in calculating check digit for Number:" + pNumber);
        }
        StringBuilder result = new StringBuilder("");
        for (int i = 0; i < pNumber.length(); i++) {
            int multiplier = ((i + 1) % 2) + 1;
            result.append(Integer.parseInt(pNumber.subSequence(i, i + 1).toString()) * multiplier);
        }
        int resultSum = 0;
        for (int i = 0; i < result.length(); i++) {
            resultSum += Integer.parseInt(result.subSequence(i, i + 1).toString());
        }
        resultSum = resultSum % 10;
        if (resultSum == 0) {
            return resultSum;
        } else {
            return 10 - resultSum;
        }
    }

}
