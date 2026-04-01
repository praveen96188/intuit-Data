package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.primary.SpcfMoney;

import com.paycycle.eft.FieldId;

@SuppressWarnings("unused")
public class Txp_CA_UI extends TxpRecordManager {

    private static String[] CA_SUI_TAX_LAW = {"87"};
    private static String[] CA_ETT_TAX_LAW = {"142"};

    public Txp_CA_UI() {
        super("/pay-def-ca-ui.xml");
    }


    public void createTxpRecord(EntryDetailRecord pEdr) {

        // Numeric digits from State Tax ID.
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getDigitsOnly(getAgencyId(pEdr)));

        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, "01300");

        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));

        // Extract the SUI tax from the specific law.
        SpcfMoney suiTax = getIndividualTaxAmount(pEdr, CA_SUI_TAX_LAW);
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(suiTax, 8));

        // Extract the ETT tax from the specific law.
        SpcfMoney ettTax = getIndividualTaxAmount(pEdr, CA_ETT_TAX_LAW);
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, formatAmount(ettTax, 8));

        // Other 2 will be the total.
        getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
