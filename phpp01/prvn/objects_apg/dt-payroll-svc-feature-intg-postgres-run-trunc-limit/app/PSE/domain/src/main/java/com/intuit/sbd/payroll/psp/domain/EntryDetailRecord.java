package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.ITxpRecordManager;
import com.intuit.sbd.payroll.psp.util.NACHAStringEncoder;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import com.intuit.sbd.payroll.psp.query.Criterion;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hand-written business logic
 */
public class EntryDetailRecord extends BaseEntryDetailRecord {
    public static final String ADDENDA_RECORD_INDICATOR_N = "0";
    public static final String ADDENDA_RECORD_INDICATOR_Y = "1";
    public static final String RECORD_TYPE_CODE = "6";
    public static final String ZERO_CREDIT_ACCOUNT_CODE = "4";
    public static final String DEFAULT_DISCRETIONARY_DATA = "  ";
    // NACHA-specified lengths
    public static final int DISCRETIONARY_DATA_LENGTH = 2;
    public static final int INDIVIDUAL_ID_LENGTH = 15;
    public static final int INDIVIDUAL_NAME_LENGTH = 22;
    public static final int ACCOUNT_NUMBER_LENGTH = 17;
    public static final int ROUTING_NUMBER_LENGTH = 9;
    public static final int AMOUNT_LENGTH = 10;//Trace number prepend
    public static final String TRACE_NUM_PREPEND = "02100002";//Maximum dollar amount for a single entry detail record
    public static final SpcfMoney NACHA_MAX_ENTRY_DETAIL_AMOUNT = new SpcfMoney("99999999.99");//    public static final SpcfMoney NACHA_MAX_ENTRY_DETAIL_AMOUNT = new SpcfMoney("200.00");
    public static final SpcfMoney SPCF_MONEY_ZERO = new SpcfMoney("0");

    public static final String TPA_ACCOUNT_NUMBER = "A100035";

    public final static String MN_UI_PAYMENT_TEMPLATE= "MN-DEED1-PAYMENT";
    public final static String CT_PFML_PAYMENT_TEMPLATE= "CT-PFML-PAYMENT";
    public final static String CT_2MAG_PAYMENT_TEMPLATE= "CT-2MAG-PAYMENT";
    public final static String MA_PFML_PAYMENT_TEMPLATE="MA-PFML-PAYMENT";
    public final static String MN_UI_AGENT_ID = "COMRE";
    public final static String MN_UI_RECIEVING_COMPANY_NAME = "MN DEED UI PAY";
    public static String RecordDataKeyName="EDR_RecordData";
    public static String TxpRecordDataKeyName="EDR_TXPRecordData";

    private static final SpcfLogger logger = Application.getLogger(EntryDetailRecord.class);

    private transient int mSequence = 0;

    public int getSequence() {
        return mSequence;
    }

    public void setSequence(int pSequence) {
        mSequence = pSequence;
    }

    public void createRecordData(FinancialTransaction pFinancialTransaction,
                                 CreditDebitCode pCreateRecordDataForCredit) {
        StringBuilder recordData = new StringBuilder();
        BankAccount bankAccountForRecordData;
        String bankAccountNumber = null;
        String individualID = null;
        String individualName = null;
        String discretionaryData = DEFAULT_DISCRETIONARY_DATA;

        // create the TXP tax payment record if appropriate
        createTxpRecordIfMeetsCriteria(pFinancialTransaction,
                                       pCreateRecordDataForCredit);

        if (NACHAFileType.CCDPlus.equals(getNACHAFileType())) {
            bankAccountNumber = getStateSpecificAccountNumber(pFinancialTransaction);
            individualID = doStateSpecificIdChanges(pFinancialTransaction);
            individualName = doStateSpecificNameChanges(pFinancialTransaction);
            discretionaryData = getStateSpecificDiscretionaryData(discretionaryData, pFinancialTransaction);
        }

        if (pCreateRecordDataForCredit.equals(CreditDebitCode.Credit)) {
            bankAccountForRecordData = pFinancialTransaction.getCreditBankAccount();

            if (individualID == null || individualName == null) {
                individualID = pFinancialTransaction.getIndividualIDOnCreditBankAccount();
                individualName = pFinancialTransaction.getNameOnCreditBankAccount();
            }
        } else {
            bankAccountForRecordData = pFinancialTransaction.getDebitBankAccount();

            if (individualID == null || individualName == null) {
                individualID = pFinancialTransaction.getIndividualIDOnDebitBankAccount();
                individualName = pFinancialTransaction.getNameOnDebitBankAccount();
            }
        }

        //Convert the amount to a BigDecimal, which is required by the StringFormatter
        SpcfMoney amount = getAmount();
        BigDecimal bdTxnAmount = new BigDecimal(amount.toString());
        if (bankAccountNumber == null) {
            bankAccountNumber = bankAccountForRecordData.getAccountNumber().replaceAll("\\s+", "");
        }

        //Create and format record
        recordData.append(RECORD_TYPE_CODE);
        String transactionCode =  getTransactionCode(pCreateRecordDataForCredit, bankAccountForRecordData.getACHAccountTypeCd());
        if (amount.isZero()) {
            transactionCode = transactionCode.substring(0,1) + ZERO_CREDIT_ACCOUNT_CODE;
        }
        recordData.append(transactionCode);
        recordData.append(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(bankAccountForRecordData.getRoutingNumber()), ROUTING_NUMBER_LENGTH));
        recordData.append(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(bankAccountNumber), ACCOUNT_NUMBER_LENGTH));
        recordData.append(StringFormatter.formatCurrencyNoDecimalPoint(bdTxnAmount, AMOUNT_LENGTH));
        recordData.append(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(individualID), INDIVIDUAL_ID_LENGTH));
        recordData.append(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(individualName), INDIVIDUAL_NAME_LENGTH));
        recordData.append(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(discretionaryData), DISCRETIONARY_DATA_LENGTH));

        // Set the addenda record indicator
        String txpRecord = getTxpRecordData();
        if ((txpRecord == null) || (txpRecord.trim().length() == 0)) {
            recordData.append(ADDENDA_RECORD_INDICATOR_N); // no addenda record
        } else {
            recordData.append(ADDENDA_RECORD_INDICATOR_Y); // has TXP addenda record
        }

        setRecordData(recordData.toString());
    }

    private static String getStateSpecificAccountNumber(FinancialTransaction pFinancialTransaction) {
        String accountNumber = null;

        String paymentTemplateCd = pFinancialTransaction.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();
        if (paymentTemplateCd.equals("IA-600103-PAYMENT") ) {
            //Iowa SUI uses a bank account number.
            DomainEntitySet<CompanyPaymentTemplateAgencyId> companyPaymentTemplateAgencyIds = pFinancialTransaction.getMoneyMovementTransaction().getCompanyPaymentMethod().getCompanyAgencyPaymentTemplate()
                    .getCompanyPaymentTemplateAgencyIdCollection().find(CompanyPaymentTemplateAgencyId.Name().equalTo("Client Bank Acct"));
            if (companyPaymentTemplateAgencyIds.size() > 0) {
                accountNumber = companyPaymentTemplateAgencyIds.getFirst().getAgencyTaxpayerId();
            }
        }

        return accountNumber;
    }

    /**
     * Gets the state specific discretionary data.
     * @param currDiscretionaryData
     * @param pFinancialTransaction The FinancialTransaction with the state
     * @return The state specific discretionary data
     */
    private static String getStateSpecificDiscretionaryData(String currDiscretionaryData, FinancialTransaction pFinancialTransaction) {

        String paymentTemplateCd = pFinancialTransaction.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();

        if (paymentTemplateCd.equals("NV-NUCS4072-PAYMENT")) {
            // Nevada SUI
            currDiscretionaryData = "NV";
        } else if (paymentTemplateCd.equals(MN_UI_PAYMENT_TEMPLATE)) {
            currDiscretionaryData = "00" ;
        }else if(paymentTemplateCd.equals(MA_PFML_PAYMENT_TEMPLATE)){
            currDiscretionaryData="AN";
        }



        return currDiscretionaryData;
    }

    /**
     * Gets the state specific Id
     * @param pFinancialTransaction The FinancialTransaction with the state
     * @return The state specific id
     */
    private String doStateSpecificIdChanges(FinancialTransaction pFinancialTransaction) {
        Company company = pFinancialTransaction.getCompany();
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company,pFinancialTransaction.getMoneyMovementTransaction().getPaymentTemplate());
        String stateTaxId = capt.getAgencyTaxpayerId();
        String paymentTemplateCd = pFinancialTransaction.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();
        String id = null;

        if (paymentTemplateCd.startsWith("OR")) {
            // OR - Zero fill from the left  + state tax ID to populate 15 digits
            id = stateTaxId.replace("-", "");
            id = StringFormatter.formatString(id, 15, '0', true);
        } else if (paymentTemplateCd.equals("NV-NUCS4072-PAYMENT")) {
            // NV SUI - 1st 9 digits of state tax ID, left padded with 6 0's to a total of 15 digits.
            id = "000000" + getFormattedAgencyIdForNV(stateTaxId);
        } else if (paymentTemplateCd.equals("ND-SFN41263-PAYMENT")) {
            // ND SUI - "00000000" + 1st 7 digits of state tax ID.
            id = "00000000" + stateTaxId.substring(0,7);
        } else if (paymentTemplateCd.equals("MO-941-PAYMENT")) {
            // MO Withholding - "0115000" + state tax ID
            id = "0115000" + stateTaxId;
        } else if (paymentTemplateCd.equals("LA-L1-PAYMENT")) {
            // LA Withholding - state tax ID
            id = stateTaxId;
        } else if (paymentTemplateCd.equals("IL-UI340-PAYMENT")) {
            // IL SUI - "ILUI" + state tax ID
            id = "ILUI" + stateTaxId;
        } else if (paymentTemplateCd.equals("AL-CR4WH-PAYMENT")) {
            // AL Withholding -
            //    If state tax ID is 6 bytes long - "000IW0000" + state tax ID
            //    If state tax ID is 10 bytes long - "000IW" + state tax ID
            if (stateTaxId.length() == 6) {
                id = "000IW0000" + stateTaxId; 
            } else if (stateTaxId.length() == 10) {
                id = "000IW" + stateTaxId;
            } else {
                logger.error("AL state tax id did not match the expected length of 6 or 10 chars.  The state tax id is " + stateTaxId +
                        " and PSID is " + company.getSourceCompanyId());
                id = "000IW" + stateTaxId;
            }
        } else if (paymentTemplateCd.equals("KY-K1-PAYMENT")) {
            // KY Withholding - "011" + 1st 6 digits of state tax ID
            if (stateTaxId.length() >= 6) {
                id = "011" + stateTaxId.substring(0, 6);
            } else {
                logger.error("KY state tax id did not match the expected length of 6 chars.  The state tax id is " + stateTaxId +
                        " and PSID is " + company.getSourceCompanyId());
                id = "011" + stateTaxId;
            }
        } else if (paymentTemplateCd.equals("MT-MW1-PAYMENT")) {
            // MT Withholding - 1st 10 digits of state tax ID + "WTH"
            stateTaxId = stateTaxId.replace("-", "");
            if (stateTaxId.length() >= 10) {
                id = stateTaxId.substring(0, 10) + "WTH";
            } else {
                logger.error("MT state tax id did not match the expected length of 10 chars.  The state tax id is " + stateTaxId +
                        " and PSID is " + company.getSourceCompanyId());
            }
        } else if (paymentTemplateCd.equals("MA-1700HI-PAYMENT")) {
            // MA SUI - "E" + state tax ID.
            id = "E" + stateTaxId;
        } else if (paymentTemplateCd.equals("MA-M941-PAYMENT")) {
            // MA Withholding - Use FEIN
            id = company.getFedTaxId();
        } else if (paymentTemplateCd.equals("AZ-UC018-PAYMENT")) {
            // AZ SUI - 1st 7 digits of state tax ID + "00000000"
            id = stateTaxId.substring(0,7) + "00000000";
        } else if (paymentTemplateCd.equals("ID-910-PAYMENT") || (paymentTemplateCd.equals("CT-PFML-PAYMENT"))){
            // ID Withholding - Use FEIN
            id = company.getFedTaxId();
        } else if (paymentTemplateCd.equals("PA-UC2-PAYMENT")) {
            id = stateTaxId;
        } else if (paymentTemplateCd.equals("VT-C101-PAYMENT")) {
            id = stateTaxId;
        } else if (paymentTemplateCd.equals(MN_UI_PAYMENT_TEMPLATE)) {
            id = MN_UI_AGENT_ID;
        } else if(paymentTemplateCd.equals(MA_PFML_PAYMENT_TEMPLATE) || paymentTemplateCd.equals("MO-MODES-PAYMENT") ) {
            id = stateTaxId.replace("-", "");
        } else if (paymentTemplateCd.equals(CT_2MAG_PAYMENT_TEMPLATE) || paymentTemplateCd.equals("CT-2MAG-PAYMENT")){
            id = stateTaxId.replace("-", "");
        }
        else {
            id = company.getSourceCompanyId();
        }

        return id;
    }


    //State Agency id
    public static String getFormattedAgencyIdForNV(String stateTaxId){
        if(stateTaxId == null)
        {
             return "";
        }
        Pattern pattern = Pattern.compile("^0\\d{8}");
        Matcher validNewNVFormat = pattern.matcher(stateTaxId);


        return validNewNVFormat.matches()  ? getDigitsOnly(stateTaxId).substring(0,9) : "0" + getDigitsOnly(stateTaxId).substring(0, 8);
    }

    /** Regex for UT punctuation */
    private static Pattern utPunctuation = Pattern.compile("~|�|!|@|#|%|&|\\*|\\(|\\|-|_|\\+|=|\\{|\\}|\\||\\|;|:|`|�|,|<|\\.|>|\\?|�|�");
    
    /**
     * Gets the state specific name
     * @param pFinancialTransaction The FinancialTransaction with the state
     * @return The state specific name
     */
    private String doStateSpecificNameChanges(FinancialTransaction pFinancialTransaction) {
        String paymentTemplateCd = pFinancialTransaction.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();
        String name;

        if (paymentTemplateCd.equals("MD-MW506-PAYMENT")) {
            // MD Withholding - "240104 WITHHOLDING"
            name = "240104 WITHHOLDING";
        }else if (paymentTemplateCd.equals("WA-PFML-PAYMENT")) {
            name = "ESD";
        } else if (paymentTemplateCd.equals("MO-941-PAYMENT")) {
            // MO Withholding - "MO DEPT OF REVENUE"
            name = String.format("%-22s",pFinancialTransaction.getCompany().getLegalName());
        } else if (paymentTemplateCd.equals("UT-TC96-PAYMENT")) {
            // UT Withholding - Remove any punctuation from Name
            name = utPunctuation.matcher(pFinancialTransaction.getCompany().getLegalName()).replaceAll("");
        } else if (paymentTemplateCd.equals(MN_UI_PAYMENT_TEMPLATE)) {
            name = MN_UI_RECIEVING_COMPANY_NAME ;
        } else {
            name = pFinancialTransaction.getCompany().getLegalName();
        }

        return name;
    }

    public void createTxpRecordIfMeetsCriteria(FinancialTransaction pFinancialTransaction,
                                               CreditDebitCode pCreateRecordDataForCredit) {
        //
        // We need to create the TXP record for tax agency ACH payments
        // Criteria:
        // - EDR must be destined for CCDPlus
        // - EDR must be the credit side of the transaction
        // - The MMT must represent an agency tax payment (with a valid payment template)
        // - The settlement type of the agency tax payment must be ACH
        //
        if (NACHAFileType.CCDPlus.equals(getNACHAFileType()) && CreditDebitCode.Credit.equals(getCreditDebitIndicator())) {
            PaymentTemplate paymentTemplate = getMoneyMovementTransaction().getPaymentTemplate();

            if ((paymentTemplate != null) && getMoneyMovementTransaction().getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                ITxpRecordManager txpRecordManager = paymentTemplate.getTxpRecordManager();

                if (txpRecordManager != null) {
                    txpRecordManager.createTxpRecord(this, pFinancialTransaction, pCreateRecordDataForCredit);
                }
            }
        }
    }

    /**
     * Create the TXP ACH Addenda record for this EDR. Note that the trace number for the given EDR must have already
     * been assigned to properly generate the TXP Addenda record (the addenda is tied to the EDR via the EDRs
     * sequence number - which is the last 7 digits of the trace number).
     * @return The formatted TXP ACH Addenda record for this EDR.
     */
    public String getTxpAchAddendaRecord() {
        //
        // CCD only allows 1 addenda record per EDR. All addenda sequence numbers must start at 1.
        //
        // Record layout: 705[80 byte TXP EDI record]SSSSEEEEEEE
        // - 7 = ACH record type code (Addenda)
        // - 05 = Addenda type code
        // - [80 byte TXP EDI record] = TXP*****\  (right padded with spaces out to 80 bytes)
        // - SSSS = 4 digit addenda sequence number (always 0001)
        // - EEEEEEE = 7 digit EDR sequence number (associates this addenda record to the EDR)
        //
        // Example 94-byte ACH TXP addenda record:
        // 705TXP*1234567890*01100*110131*T*0000013055\                                       00010000001
        //

        String txpRecord = getTxpRecordData();

        if (txpRecord == null) {
            return null;
        }

        //To calculcate the QTD wages amount to be returned in the Txp record for CT-PFML-PAYMENT
        if(this.getMoneyMovementTransaction().getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit
                && this.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd().equals("CT-PFML-PAYMENT")){

            SpcfMoney qtdTaxableWages = this.getQuarterlyWagesForCTPFML(this.getMoneyMovementTransaction());

            String txpQtdTaxableWages = changeAmountFormat(qtdTaxableWages,9);

            txpRecord = txpRecord + txpQtdTaxableWages;
        }

        txpRecord = txpRecord.trim();

        if (txpRecord.length() == 0) {
            return null;
        }

        String edrTraceNumber = getTraceNumber();

        if (edrTraceNumber == null) {
            throw new RuntimeException("Invalid EDR trace number (null).");
        }

        edrTraceNumber = edrTraceNumber.trim();

        if (edrTraceNumber.length() == 0) {
            throw new RuntimeException("Invalid EDR trace number (length == 0).");
        }

        // A valid trace number is of length 15 (i.e. 021000020000012):
        // * Positions 00 - 07 = Routing Number of ODFI (02100002)
        // * Positions 08 - 14 = EDR Sequence Number (0000012)
        // PSP saves trace numbers without the leading zeros, so we need to add them (if necessary)
        // We only need the last 7 digits of the trace number (EDR Sequence Number)
        if (edrTraceNumber.length() < 7) {
            // format left pads with spaces, so replace spaces with '0'
            edrTraceNumber = String.format("%7s", edrTraceNumber).replace(' ', '0');
        }

        return String.format("705%-80s0001%s", txpRecord, edrTraceNumber.substring(edrTraceNumber.length() - 7));
    }

    public static String getTransactionCode(CreditDebitCode pCreditOrDebit, ACHBankAccountType pACHBankAccountType) {
        if (pCreditOrDebit != null && pACHBankAccountType != null) {
            DomainEntitySet<AchTransactionCode> txCodes = AchTransactionCode.findAchTransactionCode(pACHBankAccountType, pCreditOrDebit, false);
            if (txCodes.size() > 0) {
                return txCodes.get(0).getTransactionCode();
            }
        }

        return null;
    }

    public static Long getNextTraceNumber() {
        return Application.nextSequenceValue(SequenceId.SEQ_TRACE_NBR, Long.class);
    }

    public static DomainEntitySet<EntryDetailRecord> findEntryDetailRecords(String pRecordData) {
        String probabilisticEncrypt = EncryptionUtils.probabilisticEncrypt(EntryDetailRecord.RecordDataKeyName, pRecordData, SpcfUniqueId.EmptyGuid);
        return Application.find(EntryDetailRecord.class, RecordDataEnc().equalTo(probabilisticEncrypt));
    }

    public static Long findMaxTraceNumberForFile(NACHAFile pNACHAFile) {
        String[] paramNames = new String[1];
        Object[] paramValues = new Object[1];

        paramNames[0] = "pNachaFile";
        paramValues[0] = pNACHAFile;

        List<Long> retList = Application.executeNamedQuery("findMaxTraceNumberForNachaFile", paramNames, paramValues);
        return retList.get(0);
    }

    public static EntryDetailRecord findEntryDetailRecordsWithTraceNumber(Long pTraceNumber) throws RuntimeException {
        DomainEntitySet<EntryDetailRecord> entryDetailRecords =
                Application.find(EntryDetailRecord.class,
                        TraceNumber().equalTo(pTraceNumber.toString()));

        //For Existing DD Transactions there is only one MoneyMovement Transaction Per a Trace Number
        if (entryDetailRecords.size() >= 1) {
            return entryDetailRecords.get(0);
        }
        return null;
    }

    public static EntryDetailRecord createEntryDetailRecord(CreditDebitCode pDirection, SpcfDecimal pAmount, IntuitBankAccount pIba,
                                                            MoneyMovementTransaction pMoneyMovementTransaction, FinancialTransaction pTransFt){
        // create the entity
        EntryDetailRecord edr = new EntryDetailRecord();
        edr.setNACHABatchType(NACHABatchType.TaxPayment);
        edr.setNACHAFileType(NACHAFileType.CCDPlus);
        edr.setCreditDebitIndicator(pDirection);
        edr.setAmount(new SpcfMoney(pAmount));
        edr.setCompany(pMoneyMovementTransaction.getCompany());
        edr.setIntuitBankAccount(pIba);
        edr.setMoneyMovementTransaction(pMoneyMovementTransaction);
        edr.setInitiationDate(pMoneyMovementTransaction.getInitiationDate());
        edr.setSettlementDate(pMoneyMovementTransaction.getTaxEDRSettlementDate());
        edr.setTraceNumber(null);
        edr.setLegalName(getStateSpecificLegalName(pMoneyMovementTransaction.getCompany().getLegalName(),
                                                   pMoneyMovementTransaction.getCompany().getFedTaxId(),
                                                   pMoneyMovementTransaction.getPaymentTemplate()));
        edr.setStandardEntryDescription(getStateSpecificEntryDescription(pMoneyMovementTransaction.getPaymentTemplate()));

        // if this account is NOT an INTU account, then we'll need to generate record data
        if (pIba == null && pTransFt != null) {
            edr.createRecordData(pTransFt, pDirection);
        }

        edr = Application.save(edr);
        return edr;
    }

    /**
     * Handles state specific changes to company name
     * @param currRecordCompanyLegalName The company's legal name
     * @param pt the payment template
     * @return The company name and, if applicable, any state specific changes to that name
     */
    private static String getStateSpecificLegalName(String currRecordCompanyLegalName, String fedTaxId, PaymentTemplate pt) {
        if (pt != null) {
            String stateReportName = pt.getPaymentTemplateAbbrev();

            if (stateReportName.equals("DE-DES-PAYMENT")) {
                // DE Withholding - Company name is "CRI" for DE - PSRV002922
                currRecordCompanyLegalName = "CRI";
            } else if (stateReportName.equals("MA-1700HI-PAYMENT")) {
                // MA UI - First 16 chars of ER name, TPA Account, 1+Fed Tax Id.
                // Right pad to 16 spaces.
                if (currRecordCompanyLegalName.length() > 16) {
                    currRecordCompanyLegalName = currRecordCompanyLegalName.substring(0, 16);
                }
                currRecordCompanyLegalName = String.format("%-16s" + TPA_ACCOUNT_NUMBER + "             1%s", currRecordCompanyLegalName, fedTaxId);
            } else if (stateReportName.equals("GA-DOL4-PAYMENT")) {
                if (currRecordCompanyLegalName.length() > 16) {
                    currRecordCompanyLegalName = currRecordCompanyLegalName.substring(0, 16);
                }
            } else if (stateReportName.equals("UT-TC96-PAYMENT")) {
                // UT Withholding - Company name must not have punctuation
                // Punctuation that AS400 removes:
                // ~  �  !  @  #  %  &  *  (  )  -  _  +  =  {  }  |  \  ;  :  `  �  ,  <  .  >  ?  �  �
                currRecordCompanyLegalName = utPunctuation.matcher(currRecordCompanyLegalName).replaceAll("");
            } else if (stateReportName.equals("ME-900ME-PAYMENT") || stateReportName.equals("ME-941C1ME-PAYMENT")) {
                // ME Withholding and UI - Company name is "Computing Resources Inc" for ME - PSRV004308
                currRecordCompanyLegalName = "COMPUTING RESOURCES";
            } else if (MN_UI_PAYMENT_TEMPLATE.equals(stateReportName)) {
                // MN UI - Company name is "Computing Resources Inc" for MN - PSP-9435
                SourcePayrollParameter nameParam = SourcePayrollParameter.findSourcePayrollParameter
                        (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterName);
                currRecordCompanyLegalName = StringUtils.isEmpty(nameParam.getParameterValue()) ? "Computing Resources Inc" : nameParam.getParameterValue();
            }
            else if (CT_PFML_PAYMENT_TEMPLATE.equals(stateReportName)) {
                // CT-PFML - Company name is "Intuit Computing Resources Inc" for CT
                currRecordCompanyLegalName = "Intuit Computing Resources Inc";
            }
        }

        return currRecordCompanyLegalName;
    }


    /**
     * Handles state specific changes to the entry description.
     * @param pt the payment template
     * @return The state/template specific change to the provided description.
     */
    private static String getStateSpecificEntryDescription(PaymentTemplate pt) {

        String entryDescription = null;

        if (pt != null) {
            String paymentTemplateCode = pt.getPaymentTemplateCd();

            if (paymentTemplateCode.equals("MN-DEED1-PAYMENT")) {
                // MN SUI - Use "MN UI PAY"
                entryDescription = "MN UI PAY";
            } else if (paymentTemplateCode.equals("NV-NUCS4072-PAYMENT")) {
                // NV SUI - Use "NV UITX CR"
                entryDescription= "NV UITX CR";
            } else if (paymentTemplateCode.equals("MA-1700HI-PAYMENT")) {
                // MA SUI - Use "MA DUA"
                entryDescription = "MA DUA";
            } else if (paymentTemplateCode.equals("AZ-UC018-PAYMENT")) {
                // AZ SUI - Use "UI TAX PAY"
                entryDescription = "UI TAX PAY";
            } else if (paymentTemplateCode.equals("ME-900ME-PAYMENT")) {
                // ME WH - Use "MAINEWH"
                entryDescription = "MAINEWH";
            } else if (paymentTemplateCode.equals("ME-941C1ME-PAYMENT")) {
                // ME UI - Use "MAINEUI"
                entryDescription = "MAINEUI";
            }else if (paymentTemplateCode.equals("WA-PFML-PAYMENT")) {
                // WA PFML CODE"
                entryDescription = "PFML";
            }
        }

        return entryDescription != null ? entryDescription : "EFT TAX PY";
    }

    public static ScrollableResults findEDRsWithMMTsForIntuitBankAccount(IntuitBankAccount pIntuitBankAccount, SpcfCalendar pInitiationDate) {
        String hqlSelect = "SELECT edr FROM com.intuit.sbd.payroll.psp.domain.EntryDetailRecord edr " +
                " JOIN FETCH edr.Company c" +
                " JOIN FETCH edr.MoneyMovementTransaction mmt" +
                " WHERE edr.IntuitBankAccount= :intuitBankAccount" +
                " AND mmt.Company.Id = c.Id" +
                " AND (edr.InitiationDate = :initiationDate)" +
                " AND (mmt.InitiationDate = :initiationDate)" +
                " AND (mmt.MoneyMovementPaymentMethod IN (:paymentMethods))" +
                " AND (mmt.Status = 'Executed')";

        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hqlSelect);
        hibernateQuery.setReadOnly(true);
        hibernateQuery.setParameter("intuitBankAccount", pIntuitBankAccount);
        hibernateQuery.setParameter("initiationDate", pInitiationDate);
        hibernateQuery.setParameterList("paymentMethods", Arrays.asList(PaymentMethod.ACHCredit));

        return hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
    }

    public static ScrollableResults findEDRsWithMMTsForIntuitBankAccountCriteria(IntuitBankAccount pIntuitBankAccount, SpcfCalendar pInitiationDate) {
        Expression<EntryDetailRecord> query = new Query<EntryDetailRecord>()
                .Where(EntryDetailRecord.IntuitBankAccount().equalTo(pIntuitBankAccount)
                        .And(EntryDetailRecord.InitiationDate().equalTo(pInitiationDate))
                        .And(EntryDetailRecord.MoneyMovementTransaction().InitiationDate().equalTo(pInitiationDate))
                        .And(EntryDetailRecord.MoneyMovementTransaction().MoneyMovementPaymentMethod().in(PaymentMethod.ACHCredit))
                        .And(EntryDetailRecord.MoneyMovementTransaction().Status().equalTo(PaymentStatus.Executed)))
                .EagerLoad(EntryDetailRecord.MoneyMovementTransaction().Company().equalTo(EntryDetailRecord.Company()))
                .EagerLoad(EntryDetailRecord.MoneyMovementTransaction().QbdtTransactionInfo().Company().equalTo(EntryDetailRecord.Company()))
                .EagerLoad(EntryDetailRecord.Company())
                .ReadOnly(true);
        return Application.findScrollable(EntryDetailRecord.class, query);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public EntryDetailRecord() {
        super();
    }

    @Override
    public void setNACHAFileType(NACHAFileType nachaFileType) {
        if (!ObjectUtils.equals(getNACHAFileType(), nachaFileType)) {
            recalculateNachaFile(getMoneyMovementTransaction(), nachaFileType);
        }

        super.setNACHAFileType(nachaFileType);
    }

    @Override
    public void setMoneyMovementTransaction(MoneyMovementTransaction moneyMovementTransaction) {
        if (!ObjectUtils.equals(getMoneyMovementTransaction(), moneyMovementTransaction)) {
            recalculateNachaFile(moneyMovementTransaction, getNACHAFileType());
        }

        super.setMoneyMovementTransaction(moneyMovementTransaction);
    }

    private void recalculateNachaFile(MoneyMovementTransaction moneyMovementTransaction, NACHAFileType nachaFileType) {
        if (moneyMovementTransaction == null
                || moneyMovementTransaction.getOffloadBatch() == null
                || moneyMovementTransaction.getTaxPaymentStatus() == TaxPaymentStatus.Ignore
                || nachaFileType == null) {
            setNACHAFile(null);
            return;
        }

        DomainEntitySet<NACHAFile> nachaFiles = moneyMovementTransaction.getOffloadBatch().getNACHAFilesForOffloadBatch(NACHAFileStatus.InProcess).find(NACHAFile.FileType().equalTo(nachaFileType));
        if (nachaFiles.size() == 0) {
            Application.getLogger(EntryDetailRecord.class).log(OffloadBatch.getOffloadBatchChangeLogLevel(), "Could not find pending nacha file for offload batch " + moneyMovementTransaction.getOffloadBatch().toString() + " and type " + nachaFileType.toString());

            // Find non-pending nacha file
            nachaFiles = moneyMovementTransaction.getOffloadBatch().getNACHAFileCollection().find(NACHAFile.FileType().equalTo(nachaFileType));
            if (nachaFiles.size() > 0) {
                Application.getLogger(EntryDetailRecord.class).log(OffloadBatch.getOffloadBatchChangeLogLevel(), "Associating edr " + this.getId() + " with a non-pending nacha file: " + nachaFiles.get(0).getId() + " Status: " + nachaFiles.get(0).getStatus().toString());
            }
            else {
                throw new RuntimeException("Could not find nacha file for offload batch " + moneyMovementTransaction.getOffloadBatch().toString() + " and type " + nachaFileType.toString());
            }
        }
        if (nachaFiles.size() > 1) {
            throw new RuntimeException("More than one nacha file for offload batch " + moneyMovementTransaction.getOffloadBatch().toString() + " and type " + nachaFileType.toString());
        }

        setNACHAFile(nachaFiles.get(0));
    }

    public void recalculateNachaFile() {
        recalculateNachaFile(getMoneyMovementTransaction(), getNACHAFileType());
    }

    // Copied from TxpRecordManager.java.
    private static String getDigitsOnly(String pString) {
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
    /**
     *  Company Discretionary Data will be "A" + AGENT ID
     * @param paymentTemplateCd
     * @return
     */
    public static String getStateSpecificCompanyDiscretionaryData(String paymentTemplateCd) {
        if (MN_UI_PAYMENT_TEMPLATE.equals(paymentTemplateCd)) {
            return "A" + MN_UI_AGENT_ID;
        }
        if (CT_2MAG_PAYMENT_TEMPLATE.equals(paymentTemplateCd)) {
            return "TPA";
        }
        return null;
    }
    public void setRecordData(String pRecordData) {
        super.setRecordDataEnc(EncryptionUtils.probabilisticEncrypt(RecordDataKeyName,pRecordData,getId().toString()));
    }

    public String getRecordData() {
        return EncryptionUtils.probabilisticDecrypt(RecordDataKeyName,getRecordDataEnc());
    }

    public void setTxpRecordData(String pTxpRecordData) {
        super.setTxpRecordDataEnc(EncryptionUtils.probabilisticEncrypt(TxpRecordDataKeyName,pTxpRecordData,getId().toString()));
    }

    public String getTxpRecordData() {
        return EncryptionUtils.probabilisticDecrypt(TxpRecordDataKeyName,getTxpRecordDataEnc());
    }

    public SpcfMoney getQuarterlyWagesForCTPFML(MoneyMovementTransaction moneyMovementTransaction){

        SpcfMoney qtdTaxableWages= SpcfMoney.ZERO;

        int qtr =((moneyMovementTransaction.getPaymentPeriodEnd().getMonth() - 1) / 3) + 1;
        int year = moneyMovementTransaction.getPaymentPeriodEnd().getYear();

        Criterion<EmployeeLawQtrTotals> where = EmployeeLawQtrTotals.Company().SourceCompanyId().equalTo(moneyMovementTransaction.getCompany().getSourceCompanyId())
                        .And(EmployeeLawQtrTotals.Quarter().equalTo(qtr)
                        .And(EmployeeLawQtrTotals.Year().equalTo(year))
                        .And(EmployeeLawQtrTotals.Law().LawId().equalTo("218")));
        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotals = Application.find(EmployeeLawQtrTotals.class, new Query<EmployeeLawQtrTotals>().Where(where));

       for(EmployeeLawQtrTotals elQtr : employeeLawQtrTotals){
            qtdTaxableWages = (SpcfMoney) qtdTaxableWages.add(elQtr.getTaxableWages());
        }
        return qtdTaxableWages;
    }

    public String changeAmountFormat(SpcfMoney amount, int dollarsDigits) {
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

}
