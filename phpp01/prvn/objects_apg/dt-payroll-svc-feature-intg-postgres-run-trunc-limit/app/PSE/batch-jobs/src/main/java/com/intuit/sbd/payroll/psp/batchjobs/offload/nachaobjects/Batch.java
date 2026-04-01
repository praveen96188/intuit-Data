/*
 * $Id: //psp/dev/PSE/BatchJobs/src/com/intuit/sbd/payroll/psp/batchjobs/offload/nachaobjects/Batch.java#5 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload.nachaobjects;

import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.NACHAStringEncoder;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class Batch {
    private static String COMPANY_IDENTIFICATION_CCD;
    private static String COMPANY_IDENTIFICATION_PPD;
    private static String COMPANY_IDENTIFICATION_CCDPLUS;

    private static String ORIGINATING_DFI_ID;

    private static final String ACH_FILE_EOL;
    private static final String JPMC_ACH_FILE_EOL = "\n";
    private static final String WINDOWS_ACH_FILE_EOL = "\r\n";

    private static final String SERVICE_CLASS_CODE_MIXED = "200";
    private static final String SERVICE_CLASS_CODE_CREDITS_ONLY = "220";
    private static final String SERVICE_CLASS_CODE_DEBITS_ONLY = "225";

    //BATCH HEADER AND FOOTER RECORD CONSTANTS
    private static final String BATCH_HEADER_REC_TYPE_CODE = "5";
    private static final String BATCH_FOOTER_REC_TYPE_CODE = "8";

    private static final String CCD_STANDARD_ENTRY_CLASS_CODE = "CCD";
    private static final String PPD_STANDARD_ENTRY_CLASS_CODE = "PPD";
    private static final String CCDPLUS_STANDARD_ENTRY_CLASS_CODE = "CCD";
    private static final String ORIGINATOR_STATUS_CODE = "1";
    private static final String ACH_DATE_FORMAT = "yyMMdd";

    //BATCH HEADER AND FOOTER RECORD CONSTANT LENGTHS
    private static final int COMPANY_ENTRY_DESCRIPTION_LENGTH = 10;
    private static final int COMPANY_NAME_LENGTH = 27;
    private static final int PAYROLL_ID_LENGTH = 9;
    private static final int COMPANY_ID_LENGTH = 10;
    private static final int SETTLEMENT_DATE_LENGTH = 3;
    private static final int BATCH_NUMBER_LENGTH = 7;
    private static final int ORIGINATING_DFI_ID_LENGTH = 8;
    private static final int BATCH_FOOTER_RESERVED_LENGTH = 6;
    private static final int MESSAGE_AUTHENTICATION_CODE_LENGTH = 19;

    // MA UI Company Name pattern that will replace all of fields 3, 4, and 5.
    private static Pattern MA_UI_PATTERN = Pattern.compile("^.{16}" + EntryDetailRecord.TPA_ACCOUNT_NUMBER + "\\s{13}1[0-9]{9}$");

    private TreeMap<String, String> entryDetailRecords;
    private HashMap<String, String> addendaRecords;
    private boolean hasDebits;
    private boolean hasCredits;
    private NACHABatchType batchType;
    private int numEntryDetailRecords;
    private int numRecordsInBatch;
    private int numAddendaRecords;
    private String companyName;
    private String payrollId;
    private String psid;
    private int batchNumber;
    private SpcfCalendar batchDate;
    private NACHAFileType fileType;
    private NACHATotals batchTotals;
    private String standardEntryDescription;
    private String paymentTemplateCd;
    private FundingModel fundingModel;
    private SpcfCalendar batchSettlementDate;

    static {
        if ("true".equalsIgnoreCase(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_offload_use_bank_line_end_char"))) {
            ACH_FILE_EOL = JPMC_ACH_FILE_EOL;
        } else {
            ACH_FILE_EOL = WINDOWS_ACH_FILE_EOL;
        }
    }

    //Default constructor

    /**
     * Default constructer inits passed values
     *
     * @param pBatchType   Batch Type
     * @param pCompanyName Company Name for the batch (Legal Name)
     * @param pPayrollId   Payroll ID for the batch (FEIN)
     * @param pPSID        The PSID for the company
     * @param pBatchDate   Offload date NOT the date that will appear on the batch.  That is calculated during writing to the file
     * @param pFileType    Type of file this batch will be a part of
     * @param pStandardEntryDescription Standard entry description
     */
    public Batch(NACHABatchType pBatchType, String pCompanyName, String pPayrollId, String pPSID, SpcfCalendar pBatchDate,
                 NACHAFileType pFileType, String pStandardEntryDescription) {
        populateSystemParameterValues();

        //init
        numEntryDetailRecords = 0;
        numAddendaRecords = 0;
        hasDebits = false;
        hasCredits = false;
        batchTotals = new NACHATotals();
        entryDetailRecords = new TreeMap<String, String>();
        addendaRecords = new HashMap<String, String>();

        batchType = pBatchType;
        companyName = pCompanyName;
        payrollId = pPayrollId;
        psid = pPSID;
        batchDate = pBatchDate;
        fileType = pFileType;

        standardEntryDescription = pStandardEntryDescription;
    }

    /**
     * Calls default constructor and sets batch totals
     *
     * @param pBatchType   Batch Type
     * @param pCompanyName Company Name for the batch (Legal Name)
     * @param pPayrollId   Payroll ID for the batch (FEIN)
     * @param pPSID        The PSID for the company
     * @param pBatchDate   Offload date NOT the date that will appear on the batch.  That is calculated during writing to the file
     * @param pFileType    Type of file this batch will be a part of
     * @param pBatchTotals Batch totals associated with the batch
     * @param pStandardEntryDescription Standard entry description
     */
    public Batch(NACHABatchType pBatchType, String pCompanyName, String pPayrollId, String pPSID, SpcfCalendar pBatchDate,
                 NACHAFileType pFileType, NACHATotals pBatchTotals, String pStandardEntryDescription) {
        //call default constructor
        this(pBatchType, pCompanyName, pPayrollId, pPSID, pBatchDate, pFileType, pStandardEntryDescription);
        batchTotals = pBatchTotals;
    }

    public void setBatchNumber(int pBatchNum) {
        batchNumber = pBatchNum;
    }

    public NACHATotals getBatchTotals() {
        return batchTotals;
    }

    public int getNumberOfEntryDetailRecords() {
        return numEntryDetailRecords;
    }

    public int getNumberOfAddendaRecords() {
        return numAddendaRecords;
    }

    public int getTotalNumberOfRecordsInBatch() {
        return numRecordsInBatch;
    }

    /**
     * Adds an entry detail record the batch
     *
     * @param pEntryDetailRecord The String representation of the entry detail record
     * @param pCreditDebitCode   Whether this entry detail record is a credit or a debit
     * @param pTraceNumber       Trace Number
     */
    public void addEntryDetailRecord(String pEntryDetailRecord, CreditDebitCode pCreditDebitCode, String pTraceNumber) {
        //Update the hasCredits or hasDebits boolean for use in calculating the service class code
        if (CreditDebitCode.Credit.equals(pCreditDebitCode)) {
            hasCredits = true;
        } else if (CreditDebitCode.Debit.equals(pCreditDebitCode)) {
            hasDebits = true;
        }

        //Add the entry detail records to the master list, and increment the number of entry detail records and total records in the batch
        entryDetailRecords.put(pTraceNumber, pEntryDetailRecord);
        numEntryDetailRecords++;
        numRecordsInBatch++;
    }

    /**
     * Adds an Addenda record to the batch
     *
     * @param pEntryDetailRecord entry detail record
     */
    public void addAddendaRecord(EntryDetailRecord pEntryDetailRecord) {

        //Add the addenda record
        StringBuilder strAddendaRecord = new StringBuilder();
        if (pEntryDetailRecord.getTxpAchAddendaRecord() != null) {
            strAddendaRecord.append(pEntryDetailRecord.getTxpAchAddendaRecord().trim());
        }
        strAddendaRecord.append(ACH_FILE_EOL);
        addendaRecords.put(pEntryDetailRecord.getTraceNumber(), strAddendaRecord.toString());
        numAddendaRecords++;
        numRecordsInBatch++;

    }

    /**
     * @return The batch's service class code (appears in the "5" batch header record)
     */
    public String getServiceClassCode() {
        if (hasCredits && hasDebits) {
            return SERVICE_CLASS_CODE_MIXED;
        } else if (hasCredits) {
            return SERVICE_CLASS_CODE_CREDITS_ONLY;
        } else if (hasDebits) {
            return SERVICE_CLASS_CODE_DEBITS_ONLY;
        } else {
            throw new RuntimeException("Batch has no credit or debits");
        }
    }

    /**
     * Writes the batch header, the entry detail records, and the footer to the given file
     *
     * @param pWriter Intialized file writer
     * @throws IOException If there is a problem writing to the file
     */
    public void writeToFile(PgpWriter pWriter) throws IOException {
        writeBatchHeaderRecord(pWriter, null);
        for (String currTraceNumber : entryDetailRecords.keySet()) {
            pWriter.write(entryDetailRecords.get(currTraceNumber));
        }
        writeBatchFooterRecord(pWriter, null);
    }

    /**
     * Writes the batch header, the entry detail records, and the footer to the given file
     *
     * @param pWriter Initialized file writer
     * @param pFileType NACHA File Type
     * @throws IOException If there is a problem writing to the file
     */
    public void writeToFile(PgpWriter pWriter, NACHAFileType pFileType) throws IOException {
        writeBatchHeaderRecord(pWriter, pFileType);
        for (String currTraceNumber : entryDetailRecords.keySet()) {
            pWriter.write(entryDetailRecords.get(currTraceNumber));
            // If CCDPlus write Addenda Record
            if (pFileType.equals(NACHAFileType.CCDPlus)) {
                pWriter.write(addendaRecords.get(currTraceNumber));
            }
        }
        writeBatchFooterRecord(pWriter, pFileType);
    }

    /**
     * Write the batch header "5" record
     *
     * @param pWriter Initialized file writer
     * @param nachaFileType NACHA File Type
     * @throws IOException If there is a problem writing to the file
     */
    private void writeBatchHeaderRecord(PgpWriter pWriter, NACHAFileType nachaFileType) throws IOException {
        pWriter.write(BATCH_HEADER_REC_TYPE_CODE);
        if (paymentTemplateCd != null && isCT2MAGPayment()) {
            pWriter.write(SERVICE_CLASS_CODE_MIXED);
        } else {
            pWriter.write(getServiceClassCode());                      // service class code
        }


        // If the company name matches the MA UI company name pattern, it must be the entire contents of fields 3, 4, and 5.
        if (MA_UI_PATTERN.matcher(companyName).matches()) {
            String maCompanyName = companyName.substring(0,36) + COMPANY_IDENTIFICATION_CCDPLUS;
            pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(maCompanyName), 46));
        } else if (nachaFileType != null && NACHAFileType.CCDPlus.equals(nachaFileType)) {
            if(paymentTemplateCd != null && paymentTemplateCd.equals(EntryDetailRecord.MN_UI_PAYMENT_TEMPLATE)){
                //Company name  - 16 chars
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(companyName), 16));
                //Company Discretionary Data - 20 chars
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(EntryDetailRecord.getStateSpecificCompanyDiscretionaryData(paymentTemplateCd)), 20));
                //CompanyID
                pWriter.write(COMPANY_IDENTIFICATION_CCDPLUS);
            } else if(paymentTemplateCd != null && PaymentTemplate.getPaymentTemplatesWithACHCreditForState("OR").contains(paymentTemplateCd)){
                //Company name  - 16 chars
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(getTransmitterName()), 16));
                pWriter.write("           ");
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(psid), PAYROLL_ID_LENGTH));
                pWriter.write(COMPANY_IDENTIFICATION_CCDPLUS);
            } else if (paymentTemplateCd != null && isCT2MAGPayment()) {
                //Company name  - 16 chars
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(companyName), 16));
                //Company Discretionary Data - 20 chars
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(EntryDetailRecord.getStateSpecificCompanyDiscretionaryData(paymentTemplateCd)), 20));
                pWriter.write(COMPANY_IDENTIFICATION_CCDPLUS);
            } else {
                // Company Name 19 chars
                // 8 spaces
                // PSID 10 chars
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(companyName), 19));
                pWriter.write("        ");
                pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(psid), PAYROLL_ID_LENGTH));
                pWriter.write(COMPANY_IDENTIFICATION_CCDPLUS);
            }
        } else {
            // custom formatting for QBOE modelled after Assisted/DIY; company name and company id, right justified
            pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(companyName), COMPANY_NAME_LENGTH));
            pWriter.write(StringFormatter.formatString(NACHAStringEncoder.toValidNACHAFormat(payrollId), PAYROLL_ID_LENGTH));
            pWriter.write(StringFormatter.formatString(getCompanyIdentificationCode(fileType), COMPANY_ID_LENGTH));
        }

        pWriter.write(getStandardEntryClassCode(fileType));

        pWriter.write(StringFormatter.formatString(standardEntryDescription, COMPANY_ENTRY_DESCRIPTION_LENGTH));
        if (batchSettlementDate != null) {
            pWriter.write(StringFormatter.formatDate(batchSettlementDate, ACH_DATE_FORMAT));
            pWriter.write(StringFormatter.formatDate(batchSettlementDate, ACH_DATE_FORMAT));
        } else {
            pWriter.write(getFormattedDate(batchDate, batchType, fileType));
            pWriter.write(getFormattedDate(batchDate, batchType, fileType));
        }
        pWriter.write(StringFormatter.formatString(null, SETTLEMENT_DATE_LENGTH));
        pWriter.write(ORIGINATOR_STATUS_CODE);
        pWriter.write(StringFormatter.formatString(ORIGINATING_DFI_ID, ORIGINATING_DFI_ID_LENGTH));
        pWriter.write(StringFormatter.formatLong(batchNumber, BATCH_NUMBER_LENGTH));
        pWriter.write(ACH_FILE_EOL);
        numRecordsInBatch++;
    }

    /**
     * Writes the batch footer "8" record
     *
     * @param pWriter Initialized file writer
     * @param nachaFileType NACHA File Type
     * @throws IOException If there is a problem writing to the file
     */
    private void writeBatchFooterRecord(PgpWriter pWriter, NACHAFileType nachaFileType)
            throws IOException {
        BigDecimal bdTotalCreditAmount = SpcfUtils.convertToBigDecimal(batchTotals.getTotalCreditAmount());
        BigDecimal bdTotalDebitAmount = SpcfUtils.convertToBigDecimal(batchTotals.getTotalDebitAmount());

        pWriter.write(BATCH_FOOTER_REC_TYPE_CODE);
        pWriter.write(getServiceClassCode());
        pWriter.write(StringFormatter.formatLong(numEntryDetailRecords + numAddendaRecords, 6));    // entry/addenda count
        pWriter.write(StringFormatter.formatLong(batchTotals.getEntryHash(), 10));                  // entry hash
        pWriter.write(StringFormatter.formatCurrencyNoDecimalPoint(bdTotalDebitAmount, 12));  // total debit entry dollar amount
        pWriter.write(StringFormatter.formatCurrencyNoDecimalPoint(bdTotalCreditAmount, 12)); // total credit entry dollar amount

        pWriter.write(StringFormatter.formatString(getCompanyIdentificationCode(fileType), COMPANY_ID_LENGTH));

        pWriter.write(StringFormatter.formatString(null, MESSAGE_AUTHENTICATION_CODE_LENGTH));
        pWriter.write(StringFormatter.formatString(null, BATCH_FOOTER_RESERVED_LENGTH));
        pWriter.write(StringFormatter.formatString(ORIGINATING_DFI_ID, ORIGINATING_DFI_ID_LENGTH));
        pWriter.write(StringFormatter.formatLong(batchNumber, BATCH_NUMBER_LENGTH));
        pWriter.write(ACH_FILE_EOL);
        numRecordsInBatch++;
    }

    /**
     * @param pFileType File type
     * @return Entry class code
     */
    private String getStandardEntryClassCode(NACHAFileType pFileType) {
        switch (pFileType) {
            case CCD:
                return CCD_STANDARD_ENTRY_CLASS_CODE;
            case PPD:
                return PPD_STANDARD_ENTRY_CLASS_CODE;
            case CCDPlus:
                return CCDPLUS_STANDARD_ENTRY_CLASS_CODE;
            default:
                throw new RuntimeException("Invalid file type: " + pFileType);
        }

    }

    /**
     * @param pFileType File Type
     * @return Company Identification Code
     */
    private String getCompanyIdentificationCode(NACHAFileType pFileType) {
            switch (pFileType) {
                case CCD:
                    return COMPANY_IDENTIFICATION_CCD;
                case PPD:
                    return COMPANY_IDENTIFICATION_PPD;
                case CCDPlus:
                    return COMPANY_IDENTIFICATION_CCDPLUS;
                default:
                    throw new RuntimeException("Invalid file type: " + pFileType);
            }
    }

    /**
     * Calculates the batch date based on the run date.
     * On CCD and PPD Files:
     * For CCD and PPD reversals, this is 1 business day away.
     * For all other transaction types this is 2 business days away
     * On CCDPlus Files the batch date is calculated based on the days defined by
     * the value specified on ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET system parameter. Default Value: 2
     *
     * @param pRunDate   Date we're running offload for (NOT batch date)
     * @param pBatchType Batch type
     * @param pFileType  File Type
     * @return Formatted BATCH date
     */
    private String getFormattedDate(SpcfCalendar pRunDate, NACHABatchType pBatchType, NACHAFileType pFileType) {
        SpcfCalendar settlementDate = pRunDate.copy();
        if (pFileType.equals(NACHAFileType.CCDPlus)) {
            int daysToAdd =  getTaxFundingModelOffset();
            CalendarUtils.addBusinessDays(settlementDate, daysToAdd);
        } else {
            if (pFileType.equals(NACHAFileType.CCD) ||
                    (pFileType.equals(NACHAFileType.PPD) &&
                      (pBatchType.equals(NACHABatchType.Reversal) || isOneDayFundingModel()))) {
                CalendarUtils.addBusinessDays(settlementDate, 1);
            } else {
                CalendarUtils.addBusinessDays(settlementDate, 2);
            }
        }

        return StringFormatter.formatDate(settlementDate, ACH_DATE_FORMAT);
    }

    private boolean isOneDayFundingModel() {
        return fundingModel != null && FundingModel.Codes.ONE_DAY.equals(fundingModel.getFundingModelCd());
    }

    private boolean isCT2MAGPayment() {
        return paymentTemplateCd.equals(EntryDetailRecord.CT_2MAG_PAYMENT_TEMPLATE);
    }

    private Integer getTaxFundingModelOffset() {
        int systemParameterTaxOffset = SystemParameter.findIntValue(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        return systemParameterTaxOffset;
    }


    private String getTransmitterName(){
        String transmitterName = null;
        String nameParam = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterName).getParameterValue();
        transmitterName = StringUtils.isEmpty(nameParam) ? "Computing Resources Inc" : nameParam;
       return transmitterName;
    }

    private void populateSystemParameterValues() {
        //populate system parameters
        COMPANY_IDENTIFICATION_CCD = SystemParameter
                .findSystemParameter(SystemParameter.Code.JPMC_COMPANY_ID_CCD).getSystemParameterValue();
        COMPANY_IDENTIFICATION_PPD = SystemParameter
                .findSystemParameter(SystemParameter.Code.JPMC_COMPANY_ID_PPD).getSystemParameterValue();
        COMPANY_IDENTIFICATION_CCDPLUS = SystemParameter
                .findSystemParameter(SystemParameter.Code.JPMC_COMPANY_ID_CCDPLUS).getSystemParameterValue();
        ORIGINATING_DFI_ID = SystemParameter
                .findSystemParameter(SystemParameter.Code.JPMC_ORIGINATING_DFI_ID).getSystemParameterValue();
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String pPaymentTemplateCd) {
        paymentTemplateCd = pPaymentTemplateCd;
    }

    public void setFundingModel(FundingModel fundingModel) {
        this.fundingModel = fundingModel;
    }

    public void setBatchSettlementDate(SpcfCalendar batchSettlementDate) {
        this.batchSettlementDate = batchSettlementDate;
    }

}
