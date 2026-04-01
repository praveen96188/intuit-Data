package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.FundingModel;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.delimlen.DelimRecordTemplate;
import com.paycycle.eft.FieldId;
import com.paycycle.fixedlen.FieldTemplate;
import com.paycycle.util.StringUtil;

@SuppressWarnings("unused")
public class Txp_OR_UI extends TxpRecordManager {
    private static String[] OR_SUI_TAX_LAW = {"120"};
    private static String[] OR_WCOMP_ER_TAX_LAW = {"174"};
    private static String[] OR_WCOMP_EE_TAX_LAW = {"176"};
    private static String[] OR_LOC_TRIMET_TAX_LAW = {"172"};
    private static String[] OR_LOC_LCT_TAX_LAW = {"173"};

    public Txp_OR_UI() {
        super("/pay-def-or-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     *
     * @param pEdr The EDR that will own the TXP record.
     */
    @Deprecated
    public void createTxpRecord(EntryDetailRecord pEdr) {
        throw new RuntimeException("Txp_OR_UI.createTxpRecord(pEdr) should not be called any more. Contact PSP team to fix the caller");
    }

    public void createTxpRecord(EntryDetailRecord pEdr, FinancialTransaction pFinancialTransaction,
                                CreditDebitCode pCreateRecordDataForCredit) {

        // Numeric digits from State Tax ID "0" padded on left for a total of 9 digits.
        String ein = getDigitsOnly(getAgencyId(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, StringUtil.leftPad(ein, "0", 9));
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getQuarterEndDate(pEdr));

        switch (pEdr.getSequence()) {
            case 0:
                SpcfMoney suiAmt = (SpcfMoney) getIndividualTaxAmount(pEdr, OR_SUI_TAX_LAW);
                // Combine ER and EE Workers Comp.
                SpcfMoney totalWorkersComp = (SpcfMoney) getIndividualTaxAmount(pEdr, OR_WCOMP_ER_TAX_LAW).add(getIndividualTaxAmount(pEdr, OR_WCOMP_EE_TAX_LAW));

                getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(suiAmt, 1));
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT1, formatAmount(new SpcfMoney("0.00"), 1));
                getTxpTemplate().setFieldValue(FieldId.OTHER_TAX_AMOUNT2, formatAmount(totalWorkersComp, 1));

                // check if addition edr and rxp needed
                SpcfMoney totalLocAmt = (SpcfMoney) getIndividualTaxAmount(pEdr, OR_LOC_TRIMET_TAX_LAW).add(getIndividualTaxAmount(pEdr, OR_LOC_LCT_TAX_LAW));
                if (totalLocAmt.isGreaterThan(EntryDetailRecord.SPCF_MONEY_ZERO)) {
                    if (totalLocAmt.isGreaterThan(EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT)) {
                        totalLocAmt = EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT;
                    }
                    SpcfMoney totalStateAmt = (SpcfMoney) suiAmt.add(totalWorkersComp);
                    if (totalStateAmt.isGreaterThan(EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT)) {
                        totalStateAmt = EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT;
                    }
                    pEdr.setAmount(totalStateAmt);
                    createSecondEDRAndTxp(pEdr, pFinancialTransaction, pCreateRecordDataForCredit, totalLocAmt);
                }

                pEdr.setTxpRecordData(getTxpTemplate().toString());

                break;
            case 1:
                SpcfMoney triMetAmt = (SpcfMoney) getIndividualTaxAmount(pEdr, OR_LOC_TRIMET_TAX_LAW);
                SpcfMoney lctAmt = (SpcfMoney) getIndividualTaxAmount(pEdr, OR_LOC_LCT_TAX_LAW);

                DelimRecordTemplate localTemplate = getTxpTemplate().clone();

                localTemplate.setFieldValue(FieldId.TAX_TYPE_CODE, "01102");

                for (FieldTemplate field : localTemplate.getFieldList()) {
                     if (field.getId() == FieldId.TAX_AMOUNT_TYPE) {
                         field.setValue("L");
                     }
                }

                localTemplate.setFieldValue(FieldId.TAX_AMOUNT, formatAmount(triMetAmt, 1));
                localTemplate.setFieldValue(FieldId.OTHER_TAX_AMOUNT1, formatAmount(lctAmt, 1));

                pEdr.setTxpRecordData(localTemplate.toString().replace("*L*\\", "\\"));

                break;
        }
    }

    private void createSecondEDRAndTxp(EntryDetailRecord pOriginalEdr, FinancialTransaction pFinancialTransaction,
                                       CreditDebitCode pCreateRecordDataForCredit, SpcfMoney pAmount) {
        EntryDetailRecord edr2 = new EntryDetailRecord();
        edr2.setSequence(1);
        edr2.setNACHABatchType(pOriginalEdr.getNACHABatchType());
        edr2.setNACHAFileType(pOriginalEdr.getNACHAFileType());
        edr2.setCreditDebitIndicator(pOriginalEdr.getCreditDebitIndicator());
        edr2.setCompany(pOriginalEdr.getCompany());
        edr2.setIntuitBankAccount(pOriginalEdr.getIntuitBankAccount());
        edr2.setMoneyMovementTransaction(pOriginalEdr.getMoneyMovementTransaction());
        edr2.setInitiationDate(pOriginalEdr.getInitiationDate());
        edr2.setSettlementDate(pOriginalEdr.getMoneyMovementTransaction().getEDRSettlementDate());
        edr2.setTraceNumber(pOriginalEdr.getTraceNumber());
        edr2.setLegalName(pOriginalEdr.getLegalName());
        edr2.setStandardEntryDescription(pOriginalEdr.getStandardEntryDescription());

        edr2.setAmount(pAmount);

        edr2.createRecordData(pFinancialTransaction, pCreateRecordDataForCredit);

        Application.save(edr2);

        pOriginalEdr.getMoneyMovementTransaction().getEntryDetailRecordCollection().add(edr2);
    }

}
