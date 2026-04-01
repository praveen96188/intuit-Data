package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eft.FieldId;

/**
 * User: ihannur
 * Date: 6/1/12
 * Time: 10:56 AM
 */

@SuppressWarnings("unused")
public class Txp_LA_UI extends TxpRecordManager {

    public Txp_LA_UI() {
        super("/pay-def-la-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        // Numeric digits from State Tax ID - 7 digits
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));

        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());

        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndYearAndQuarter(pEdr));

        //Total Wages Paid, Net Taxable Wages - pending confirmation from agency for putting these values as zeroes
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(SpcfMoney.ZERO, 0));
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, formatAmount(SpcfMoney.ZERO, 0));

        // Other 2 will be the total.
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }

}
