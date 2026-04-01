package com.intuit.sbd.payroll.psp.batchjobs.printedchecks.printedcheckfile;

import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 5, 2011
 * Time: 4:45:38 PM
 */
public class ReconPlusFile {
    private static final String EOL = "\r\n";
    private static final String SETTLEMENT_TYPE = "CHECK";

    private static final String DATE_FORMAT = "yyyyMMdd";

    private static final String CHECK_NUMBER_FORMAT = "00000000";
    private static final String AMOUNT_FORMAT = "000000000000";

    /*  Checking Account & Common Lengths   */
    private static final int MAX_ACCOUNT_NUMBER_LENGTH = 10;
    private static final int MAX_SETTLEMENT_TYPE_LENGTH = 5;
    private static final int MAX_AGENCY_NUMBER_LENGTH = 15;
    private static final int MAX_COMPANY_NAME_LENGTH = 25;
    private static final int MAX_PSID_LENGTH = 10;
    private static final int MAX_PAYMENT_TEMPLATE_LENGTH = 20;
    private static final int MAX_CREATOR_ID_LENGTH = 15;
    private static final int MAX_EIN_LENGTH = 9;
    private static final int MAX_PAYEE_LENGTH = 30;

    /*  Tax Account Lengths */
    private static final int MAX_TRANSACTION_TYPE_LENGTH = 15;
    public static final int MAX_FILE_KEY_LENGTH = 40;
    private static final int MAX_TRANSACTION_STATUS_LENGTH = 10;

    /*  Returns Account Lengths */
    private static final int MAX_EFTPS_PAYMENT_DETAIL_LENGTH = 60;
    private static final int MAX_RETURN_DESCRIPTION_LENGTH = 30;
    private static final int MAX_RETURN_CODE_LENGTH = 5;
    private static final int MAX_DEBIT_CREDIT_INDICATOR_LENGTH = 8;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
    private DecimalFormat mCheckNumberFormat = new DecimalFormat(CHECK_NUMBER_FORMAT);
    private DecimalFormat mAmountFormat = new DecimalFormat(AMOUNT_FORMAT);        

    public void writeDetailRecord(PgpWriter pFileWriter, String pCreator, String pBankAccountNumber, String pCheckNumber, SpcfMoney pCheckAmount,
                                  SpcfCalendar pInitiationDate, String pPayee, String pAgencyNumber, String pCompanyLegalName,
                                  String pSourceCompanyId, String pFedTaxId, String pPaymentTemplate, int pQuarter, int pYear) throws Exception {
       if (pFileWriter == null) {
            throw new RuntimeException("Error writing recon plus check record. File Writer can not be null. Check Number: " + pCheckNumber);
        }
        if (pCreator == null) {
            throw new RuntimeException("Error writing recon plus check record. Creator Id can not be null. Check Number: " + pCheckNumber);
        }
        if (pBankAccountNumber == null) {
            throw new RuntimeException("Error writing recon plus check record. Bank Account Number can not be null. Check Number: " + pCheckNumber);
        }
        if (pCheckNumber == null) {
            throw new RuntimeException("Error writing recon plus check record. Check Number can not be null. Check Number: " + pCheckNumber);
        }
        if (pCheckAmount == null) {
            throw new RuntimeException("Error writing recon plus check record. Check Amount can not be null. Check Number: " + pCheckNumber);
        }
        if (pInitiationDate == null) {
            throw new RuntimeException("Error writing recon plus check record. Initiation Date can not be null. Check Number: " + pCheckNumber);
        }
        if (pPayee == null) {
            throw new RuntimeException("Error writing recon plus check record. Payee Name can not be null. Check Number: " + pCheckNumber);
        }
        if (pCompanyLegalName == null) {
            throw new RuntimeException("Error writing recon plus check record. Company Name can not be null. Check Number: " + pCheckNumber);
        }
        if (pSourceCompanyId == null) {
            throw new RuntimeException("Error writing recon plus check record. PSID can not be null. Check Number: " + pCheckNumber);
        }
        if (pFedTaxId == null) {
            throw new RuntimeException("Error writing recon plus check record. EIN can not be null. Check Number: " + pCheckNumber);
        }
        if (pPaymentTemplate == null) {
            throw new RuntimeException("Error writing recon plus check record. Payment Template can not be null. Check Number: " + pCheckNumber);
        }

        // account number
        pFileWriter.write(StringUtils.rightPad(pBankAccountNumber,MAX_ACCOUNT_NUMBER_LENGTH));

        // check amount
        pFileWriter.write(mAmountFormat.format(Long.parseLong(pCheckAmount.toString().replace(".",""))));

        // issued date
        pFileWriter.write(mDateFormat.format(new Date(pInitiationDate.getTimeInMilliseconds())));

        // transaction type
        pFileWriter.write(StringUtils.rightPad(SETTLEMENT_TYPE, MAX_SETTLEMENT_TYPE_LENGTH));

        // agency number
        pFileWriter.write(StringUtils.rightPad(pAgencyNumber == null ? "" : truncateString(pAgencyNumber.trim().replace("-", ""), MAX_AGENCY_NUMBER_LENGTH), MAX_AGENCY_NUMBER_LENGTH));

        // legal name
        pFileWriter.write(StringUtils.rightPad(truncateString(pCompanyLegalName, MAX_COMPANY_NAME_LENGTH), MAX_COMPANY_NAME_LENGTH));

        // psid
        pFileWriter.write(StringUtils.rightPad(pSourceCompanyId, MAX_PSID_LENGTH));

        // payment template
        pFileWriter.write(StringUtils.rightPad(truncateString(pPaymentTemplate, MAX_PAYMENT_TEMPLATE_LENGTH), MAX_PAYMENT_TEMPLATE_LENGTH));

        // tax year / quarter
        pFileWriter.write(String.format("%1$04d%2$01d", pYear, pQuarter));

        // creator
        pFileWriter.write(StringUtils.rightPad(truncateString(pCreator, MAX_CREATOR_ID_LENGTH), MAX_CREATOR_ID_LENGTH));

        // ein
        pFileWriter.write(StringUtils.rightPad(pFedTaxId, MAX_EIN_LENGTH));

        // check number
        pFileWriter.write(mCheckNumberFormat.format(Long.parseLong(pCheckNumber)));

        // payee
        pFileWriter.write(StringUtils.rightPad(truncateString(pPayee, MAX_PAYEE_LENGTH), MAX_PAYEE_LENGTH));

        pFileWriter.write(EOL);
    }

    public void writeTaxAccountsDetailRecord(PgpWriter pFileWriter, String pBankAccountNumber, SpcfMoney pAmount, SpcfCalendar pInitiationDate, SpcfCalendar pSettlementDate,
                                             String pTransactionType, String pAgencyNumber, String pCompanyLegalName, String pSourceCompanyId, String pPaymentTemplate, int pQuarter, int pYear,
                                             String pCreator, String pFedTaxId, String pFileKey,
                                             String pTransactionStatus, String pEFTPSPaymentDetail,
                                             CreditDebitCode pDCIP) throws IOException {
        if (pFileWriter == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. File Writer can't be null.");
        }
        if (pCreator == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Creator Id can't be null.");
        }
        if (pBankAccountNumber == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Bank Account Number can't be null.");
        }
        if (pAmount == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Amount can't be null.");
        }
        if (pInitiationDate == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Initiation Date can't be null.");
        }
        if (pDCIP == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. DebitCredit Indicator can't be null.");
        }
        if (pTransactionType == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Transaction Type can't be null.");
        }
        if (pCompanyLegalName == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Company Name can't be null.");
        }
        if (pSourceCompanyId == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. PSID can't be null.");
        }
        if (pFedTaxId == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. EIN can't be null.");
        }
        if (pPaymentTemplate == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Payment Template can't be null.");
        }
        if (pTransactionStatus == null) {
            throw new RuntimeException("Error writing ReconPlus Tax Account record. Transaction Status can't be null.");
        }

        // Intuit account number
        pFileWriter.write(StringUtils.rightPad(pBankAccountNumber, MAX_ACCOUNT_NUMBER_LENGTH));

        // check amount
        pFileWriter.write(mAmountFormat.format(Long.parseLong(pAmount.toString().replace(".",""))));

        // Debit/Credit Indicator
        pFileWriter.write(StringUtils.rightPad(pDCIP.toString(), MAX_DEBIT_CREDIT_INDICATOR_LENGTH));
        
        // execution date
        pFileWriter.write(mDateFormat.format(new Date(pInitiationDate.getTimeInMilliseconds())));

        // transaction type
        pFileWriter.write(StringUtils.rightPad(truncateString(pTransactionType, MAX_TRANSACTION_TYPE_LENGTH), MAX_TRANSACTION_TYPE_LENGTH));

        // agency number
        pFileWriter.write(StringUtils.rightPad((pAgencyNumber == null ? "" : truncateString(pAgencyNumber.trim().replace("-", ""), MAX_AGENCY_NUMBER_LENGTH)), MAX_AGENCY_NUMBER_LENGTH));

        // legal name
        pFileWriter.write(StringUtils.rightPad(truncateString(pCompanyLegalName, MAX_COMPANY_NAME_LENGTH), MAX_COMPANY_NAME_LENGTH));

        // psid
        pFileWriter.write(StringUtils.rightPad(pSourceCompanyId, MAX_PSID_LENGTH));

        // payment template
        pFileWriter.write(StringUtils.rightPad(truncateString(pPaymentTemplate, MAX_PAYMENT_TEMPLATE_LENGTH), MAX_PAYMENT_TEMPLATE_LENGTH));

        // tax year / quarter
        pFileWriter.write(String.format("%1$04d%2$01d", pYear, pQuarter));

        // creator
        pFileWriter.write(StringUtils.rightPad(truncateString(pCreator, MAX_CREATOR_ID_LENGTH), MAX_CREATOR_ID_LENGTH));

        // ein
        pFileWriter.write(StringUtils.rightPad(pFedTaxId, MAX_EIN_LENGTH));

        // file key
        pFileWriter.write(StringUtils.rightPad(truncateString(pFileKey, MAX_FILE_KEY_LENGTH), MAX_FILE_KEY_LENGTH));

        // transaction status
        pFileWriter.write(StringUtils.rightPad(pTransactionStatus, MAX_TRANSACTION_STATUS_LENGTH));

        // payment details
        pFileWriter.write(StringUtils.rightPad(truncateString((pEFTPSPaymentDetail == null ? "" : pEFTPSPaymentDetail), MAX_EFTPS_PAYMENT_DETAIL_LENGTH), MAX_EFTPS_PAYMENT_DETAIL_LENGTH));

        pFileWriter.write(EOL);
    }

    public void writeReturnsAccountsDetailRecord(FileWriter pFileWriter, String pBankAccountNumber, SpcfMoney pAmount,
                                                 SpcfCalendar pInitiationDate, SpcfCalendar pSettlementDate, String pFinancialTransactionType, String pTransactionType, String pReturnCode,
                                                 String pCompanyLegalName, String pSourceCompanyId, String pCreator, String pFedTaxId,
                                                 String pReturnDescription) throws IOException {
        if (pFileWriter == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. File Writer can't be null.");
        }
        if (pCreator == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Creator Id can't be null.");
        }
        if (pBankAccountNumber == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Bank Account Number can't be null.");
        }
        if (pAmount == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Amount can't be null.");
        }
        if (pInitiationDate == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Initiation Date can't be null.");
        }
        if (pSettlementDate == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Settlement Date can't be null.");
        }
        if (pFinancialTransactionType == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Financial Transaction Type can't be null.");
        }
        if (pTransactionType == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Transaction Type can't be null.");
        }
        if (pReturnCode == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Return Code can't be null.");
        }
        if (pCompanyLegalName == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Company Name can't be null.");
        }
        if (pSourceCompanyId == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. PSID can't be null.");
        }
        if (pFedTaxId == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. EIN can't be null.");
        }
        if (pReturnDescription == null) {
            throw new RuntimeException("Error writing ReconPlus Returns Account record. Return Description can't be null.");
        }

        // account number
        pFileWriter.write(StringUtils.rightPad(pBankAccountNumber,MAX_ACCOUNT_NUMBER_LENGTH));

        // check amount
        pFileWriter.write(mAmountFormat.format(Long.parseLong(pAmount.toString().replace(".",""))));

        // execution date
        pFileWriter.write(mDateFormat.format(new Date(pInitiationDate.getTimeInMilliseconds())));

        // settlement date
        pFileWriter.write(mDateFormat.format(new Date(pSettlementDate.getTimeInMilliseconds())));

        // financial transaction type
        pFileWriter.write(StringUtils.rightPad(truncateString(pFinancialTransactionType, MAX_TRANSACTION_TYPE_LENGTH), MAX_TRANSACTION_TYPE_LENGTH));

        // transaction type
        pFileWriter.write(StringUtils.rightPad(truncateString(pTransactionType, MAX_TRANSACTION_TYPE_LENGTH), MAX_TRANSACTION_TYPE_LENGTH));

        // return code
        pFileWriter.write(StringUtils.rightPad(pReturnCode, MAX_RETURN_CODE_LENGTH));

        // legal name
        pFileWriter.write(StringUtils.rightPad(truncateString(pCompanyLegalName, MAX_COMPANY_NAME_LENGTH), MAX_COMPANY_NAME_LENGTH));

        // psid
        pFileWriter.write(StringUtils.rightPad(pSourceCompanyId, MAX_PSID_LENGTH));

        // creator
        pFileWriter.write(StringUtils.rightPad(truncateString(pCreator, MAX_CREATOR_ID_LENGTH), MAX_CREATOR_ID_LENGTH));

        // ein
        pFileWriter.write(StringUtils.rightPad(pFedTaxId, MAX_EIN_LENGTH));

        // return description
        pFileWriter.write(StringUtils.rightPad(truncateString(pReturnDescription, MAX_RETURN_DESCRIPTION_LENGTH), MAX_RETURN_DESCRIPTION_LENGTH));

        pFileWriter.write(EOL);
    }

    private String truncateString(String initialString, int maxCharacters) {
        if(initialString != null && initialString.length() > maxCharacters) {
            return initialString.substring(0, maxCharacters);
        }
        return initialString;
    }
}
