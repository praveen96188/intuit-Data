package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.ITxpRecordManager;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.delimlen.DelimRecordTemplate;
import com.paycycle.util.XmlResourcePool;
import org.apache.commons.lang.StringUtils;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: May 15, 2011
 * Time: 9:52:10 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TxpRecordManager implements ITxpRecordManager {
    private XmlResourcePool mXmlResourcePool = new XmlResourcePool();
    private DelimRecordTemplate mTxpTemplate;

    protected TxpRecordManager(String mTxpDefFile) {
        mXmlResourcePool.addResource(mTxpDefFile);
        loadTxpTemplate();
    }

    public XmlResourcePool getXmlResourcePool() {
        return mXmlResourcePool;
    }

    public DelimRecordTemplate getTxpTemplate() {
        return mTxpTemplate;
    }

    public String getDefaultTemplateId() {
        return getClass().getSimpleName();
    }

    public void loadTxpTemplate() {
        loadTxpTemplate(getDefaultTemplateId());
    }

    public void loadTxpTemplate(String pTemplateId) {
        mTxpTemplate = (DelimRecordTemplate) getXmlResourcePool().get(pTemplateId, false);

        //
        // Don't want crlf since we're embedding TXP in addenda record
        //
        if (mTxpTemplate != null) {
            mTxpTemplate.setIncludeCarriageReturn(false);
        }
    }

    public String formatAmount(SpcfMoney amount, int dollarsDigits) {
        StringBuffer dollarPattern = new StringBuffer(dollarsDigits);

        for (int i = 0; i < dollarsDigits; i++) {
            dollarPattern.append( "0" );
        }

        DecimalFormat dollarsFormat = new DecimalFormat(dollarPattern.toString());
        String dollarsOutput = dollarsFormat.format(amount.getIntegerPart());
        DecimalFormat centsFormat = new DecimalFormat("00");
        String centsOutput = centsFormat.format(amount.getFractionalPart());

        return dollarsOutput + centsOutput;
    }

    public String getAgencyId(EntryDetailRecord pEdr) {
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pEdr.getCompany(),pEdr.getMoneyMovementTransaction().getPaymentTemplate());
        return capt.getAgencyTaxpayerId();
    }

    public String getEin(EntryDetailRecord pEdr) {
        return pEdr.getCompany().getFedTaxId();
    }

    public String getEndDate(EntryDetailRecord pEdr) {
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().format("yyMMdd");
    }
    public String getMonthEndDate(EntryDetailRecord pEdr) {
        return CalendarUtils.getLastDayOfMonth(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd()).format("yyMMdd");
    }
    public String getQuarterEndDate(EntryDetailRecord pEdr){
        return CalendarUtils.getLastDayOfQuarter(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd()).format("yyMMdd");
    }

    public String getEndDateYear(EntryDetailRecord pEdr) {
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().format("yyyy");
    }

    public String getEndYearAndQuarter(EntryDetailRecord pEdr) {
        //Return YYQ
        int quarter = CalendarUtils.getQuarterAsInt(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd());
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().format("yy")+String.valueOf(quarter);
    }

    public String getBeginDate(EntryDetailRecord pEdr) {
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodBegin().format("yyMMdd");
    }

    public String getFullEndDate(EntryDetailRecord pEdr) {
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().format("yyyyMMdd");
    }

    public String getFullBeginDate(EntryDetailRecord pEdr) {
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodBegin().format("yyyyMMdd");
    }

    public long getDaysForPeriod(EntryDetailRecord pEdr) {
        long days = pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().subtract(pEdr.getMoneyMovementTransaction().getPaymentPeriodBegin());
        days = days / 86400000; // Convert from milliseconds to days
        return days;
    }

    public String getDigitsOnly(String pString) {
        if (pString == null) {
            return null;
        }

        StringBuffer strBuff = new StringBuffer();
        char c;
        for (int i = 0; i < pString.length(); i++) {
            c = pString.charAt(i);
            if (Character.isDigit(c)) {
                strBuff.append(c);
            }
        }
        return strBuff.toString();
    }

    public String getWithoutHyphens(String pString) {
        if (pString == null) {
            return null;
        }

        return pString.replace("-", "");
    }

    public String getWithoutSpaces(String pString) {
        if (pString == null) {
            return null;
        }

        return pString.replace(" ", "");
    }

    public String getQuarter(EntryDetailRecord pEdr){

        int quarter=CalendarUtils.getQuarterAsInt(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd());
           return String.valueOf(quarter);

    }


    public SpcfMoney getIndividualTaxAmount(EntryDetailRecord pEdr, String[] pLawId) {
        SpcfMoney creditAmount = SpcfMoney.ZERO;
        SpcfMoney debitAmount = SpcfMoney.ZERO;
        DomainEntitySet<FinancialTransaction> creditFinancialTransactions = pEdr.getMoneyMovementTransaction().getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxCredit))
                        .And(FinancialTransaction.Law().LawId().in(pLawId))
                        .And(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Created))));
        for (FinancialTransaction creditFinancialTransaction : creditFinancialTransactions) {
            creditAmount = (SpcfMoney) creditAmount.add(creditFinancialTransaction.getFinancialTransactionAmount());
        }
        DomainEntitySet<FinancialTransaction> debitFinancialTransactions = pEdr.getMoneyMovementTransaction().getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxDebit))
                        .And(FinancialTransaction.Law().LawId().in(pLawId))
                        .And(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Created))));
        for (FinancialTransaction debitFinancialTransaction : debitFinancialTransactions) {
            debitAmount = (SpcfMoney) debitAmount.add(debitFinancialTransaction.getFinancialTransactionAmount());
        }
        return (SpcfMoney) creditAmount.subtract(debitAmount);
    }

    // Fetching Trasmitter FEIN
    public String getPayorFEIN(){
        String payorFEIN = null;
        String transmitterFEINParam = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterFEIN).getParameterValue();

        payorFEIN = transmitterFEINParam.substring(0,2) + "-" + transmitterFEINParam.substring(2);
        return payorFEIN;
    }

    public String getEinWithHyphen(EntryDetailRecord pEdr) {
        String ein = pEdr.getCompany().getFedTaxId();
        if(!StringUtils.isEmpty(ein) && ein.length() == 9) {
            ein = ein.substring(0, 2) + "-" + ein.substring(2);
        }
        return ein;
    }

    /**
     * Gets the state id or if it is not found or incorrect, returns FEIN
     *
     * @param pEdr           The EntryDetailRecord containing the data
     * @param expectedLength The expected length for the state id
     * @return The state id or FEIN
     */
    public String getStateOrFEIN(EntryDetailRecord pEdr, int expectedLength) {
        String taxId = getDigitsOnly(getAgencyId(pEdr));

        if (taxId == null || taxId.length() != expectedLength) {
            taxId = getEin(pEdr);
        }

        return taxId;
    }

    public void createTxpRecord(EntryDetailRecord pEdr, FinancialTransaction pFinancialTransaction,
                                CreditDebitCode pCreateRecordDataForCredit) {
        createTxpRecord(pEdr);
    }

    public String getWithoutSpecChars(String pString) {
        if (pString == null) {
            return null;
        }
        return pString.replaceAll("[^a-zA-Z0-9]", "");
    }
}
