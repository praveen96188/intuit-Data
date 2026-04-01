package com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Outputs NY state payment files - Refer to PSP-8219 for details<br>
 * <p/>
 * Example output line:<br>
 * 1HDR000000          010111                      000WT-1   88-014671  3Computing Resources Inc                 6884 Sierra Center Parkway    Reno                     NV89511
 * 012245678904                             000000000072000000001080000000022600                                 000000000000000004060000000040600      00
 * HASH TOTAL 0000000000000  000000
 * 1EOF 0000001
 */
public class OffloadNYDTFPayments {

    protected static final SpcfLogger logger = Application.getLogger(OffloadNYDTFPayments.class);

    // Constants for header of the file
    public static final String HEADER_LABEL_IDENTIFIER = "1HDR";
    public static final String FORM_TYPE_INDICATOR = "WT-1 ";
    public static final String SUBMITTER_IDENTIFICATION_NUMBER = "88014671100";
    public static final String SUBMITTER_CHECK_DIGIT = "3";
    public static final String SUBMITTER_NAME = "Computing Resources Inc";
    public static final String SUBMITTER_ADDRESS = "6884 Sierra Center Parkway";
    public static final String SUBMITTER_CITY = "Reno";
    public static final String SUBMITTER_STATE = "NV";
    public static final String SUBMITTER_ZIP_CODE = "89511";

    //Laws
    public static final String NY_SWT = "36";
    public static final String LWT_NYCR = "54";
    public static final String LWT_NYCNR = "55";
    public static final String LWT_YR = "56";
    public static final String LWT_YNR = "57";

    //Constants for Hash Record
    public static final String HASH = "HASH";
    public static final String TOTAL = "TOTAL";

    //Constants for Trailer Record
    public static final String TRAILER_LABEL_IDENTIFIER = "1EOF";

    //Other Constants
    public static final String ADDITIONAL_PAYMENT_INDICATOR = "0";
    public static final String EOL = "\r\n";
    public static final String FILENAME_PREFIX = "WT1";
    public static final String NY_1MN_PAYMENT = "NY-1MN-PAYMENT";
    public static final String OUTPUT_DIRECTORY;
    public static final String ARCHIVE_DIRECTORY;
    public static final String FILE_EXT = ".RPT";

    private PSPRequestContextManager pspRequestContextManager;

    public OffloadNYDTFPayments() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    static {
        OUTPUT_DIRECTORY = BatchUtils.getConfigString("psp_batch_ftp_send_dir");
        ARCHIVE_DIRECTORY = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
    }

    public class NY_DTF_DTO {
        private SpcfDecimal totalRemittancePaid;
        private int numOfDataRecordsCreated;

        public NY_DTF_DTO() {
            totalRemittancePaid = SpcfMoney.ZERO;
            numOfDataRecordsCreated = 0;
        }

        public SpcfDecimal getTotalRemittancePaid() {
            return totalRemittancePaid;
        }

        public void setTotalRemittancePaid(SpcfDecimal pTotalRemittancePaid) {
            totalRemittancePaid = pTotalRemittancePaid;
        }

        public int getNumOfDataRecordsCreated() {
            return numOfDataRecordsCreated;
        }

        public void setNumOfDataRecordsCreated(int pNumOfDataRecordsCreated) {
            numOfDataRecordsCreated = pNumOfDataRecordsCreated;
        }
    }

    public void createFiles(SpcfCalendar passedInDate) throws IOException {

        SpcfCalendar initiationDate = passedInDate.copy();
        CalendarUtils.clearTime(initiationDate);

        SpcfCalendar dueDate = initiationDate.copy();
        //Due date for the tax payments is 2 BD after initiation date

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(NY_1MN_PAYMENT);

        CalendarUtils.addBusinessDays(dueDate, MoneyMovementTransaction.getPaymentMethodDayOffset(PaymentMethod.ACHDebit, paymentTemplate));
        final int MAX_COMPANIES_PER_FILE = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_state_reports_ny_dtf_max_companies_per_file"));

        HashMap<Company, ArrayList<MoneyMovementTransaction>> companyToMoneyMovementTransactions =
                OffloadNYDTFPayments.getMoneyMovementTransactionsForInitiationDate(paymentTemplate, initiationDate);

        logger.info("Running OffloadNYDTFPayments for " + paymentTemplate.getPaymentTemplateCd() +
                            " for initiation date " + initiationDate.format("yyyy/MM/dd")
                            + " having " + companyToMoneyMovementTransactions.size() + " companies");

        //Splitting the map with MAX employers per file
        Boolean multipleFilesToBeCreated = Boolean.FALSE;
        List<String> createdFilesList = new ArrayList<String>();
        List<HashMap<Company, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactionSplitList
                = new ArrayList<HashMap<Company, ArrayList<MoneyMovementTransaction>>>();

        HashMap<Company, ArrayList<MoneyMovementTransaction>> currentCompanyToMoneyMovementTransactionMap
                = new HashMap<Company, ArrayList<MoneyMovementTransaction>>();
        companyToMoneyMovementTransactionSplitList.add(currentCompanyToMoneyMovementTransactionMap);

        for (Map.Entry<Company, ArrayList<MoneyMovementTransaction>> entry : companyToMoneyMovementTransactions.entrySet()) {

            if (currentCompanyToMoneyMovementTransactionMap.size() >= MAX_COMPANIES_PER_FILE) {
                multipleFilesToBeCreated = Boolean.TRUE;
                currentCompanyToMoneyMovementTransactionMap
                        = new HashMap<Company, ArrayList<MoneyMovementTransaction>>();
                companyToMoneyMovementTransactionSplitList.add(currentCompanyToMoneyMovementTransactionMap);
            }
            currentCompanyToMoneyMovementTransactionMap.put(entry.getKey(), entry.getValue());
        }

        for (HashMap<Company, ArrayList<MoneyMovementTransaction>> companyToMoneyMovementTransactionMap : companyToMoneyMovementTransactionSplitList) {
            //Start creation of a new file
            //if we have to create multiple files then create files like WT10713_PART1.RPT
            String filename = OUTPUT_DIRECTORY + File.separator + FILENAME_PREFIX + dueDate.format("MMdd") +
                    (multipleFilesToBeCreated ? "_PART" + createdFilesList.size() + 1 : "") + FILE_EXT;
            logger.info("Started creation of file - " + filename);
            //create new file, throwing IOException out to the parent process
            // the reason is that we want to rollback the transaction at this point
            FileWriter writer = new FileWriter(filename);
            NY_DTF_DTO dto = new NY_DTF_DTO();
            //Begin writing records
            //Write Header Record
            writeHeaderRecord(writer);
            //EOL
            writer.append(EOL);
            //Write Data Records
            writeDataRecords(writer, companyToMoneyMovementTransactionMap, dto);
            if(dto.getNumOfDataRecordsCreated() > 0) {
                //EOL
                writer.append(EOL);
            }
            //Write Hash Record
            writeHashRecord(writer, dto);
            //EOL
            writer.append(EOL);
            //Write Trailer Record
            writeTrailerRecord(writer, dto);
            //End of writing records
            //Flush and close
            writer.flush();
            writer.close();
            logger.info("Finished creation of file - " + filename);
            //Add it to the list of files created
            createdFilesList.add(filename);
        }

    }

    public void writeHeaderRecord(Appendable writer) throws IOException {
        //Start of Record
        //Pos 1-4
        writer.append(HEADER_LABEL_IDENTIFIER);
        // 5-10 Zero fill
        writer.append(ReportUtils.getPaddedWholeNumber(0, 6));
        // 11-20 Filler
        writer.append(ReportUtils.cropOrPad("", 10));
        // 21-26 Tape creation date
        writer.append(PSPDate.getPSPTime().format("MMddyy"));
        // 27-48 Filler
        writer.append(ReportUtils.cropOrPad("", 22));
        // 49-51 0 fill
        writer.append(ReportUtils.getPaddedWholeNumber(0, 3));
        // 52-55 Form type indicator
        writer.append(FORM_TYPE_INDICATOR);
        // 56-58 Filler
        writer.append(ReportUtils.cropOrPad("", 2));
        // 59-69 SIN
        writer.append(ReportUtils.cropOrPad(SUBMITTER_IDENTIFICATION_NUMBER, 11));
        // 70 SCD
        writer.append(ReportUtils.cropOrPad(SUBMITTER_CHECK_DIGIT, 1));
        // 71-110 SN
        writer.append(ReportUtils.cropOrPad(SUBMITTER_NAME, 40));
        // 111-140 SSA
        writer.append(ReportUtils.cropOrPad(SUBMITTER_ADDRESS, 30));
        // 141-165 SC
        writer.append(ReportUtils.cropOrPad(SUBMITTER_CITY, 25));
        // 166-167 SS
        writer.append(ReportUtils.cropOrPad(SUBMITTER_STATE, 2));
        // 168 - 176 SZC
        writer.append(ReportUtils.cropOrPad(SUBMITTER_ZIP_CODE, 9));
        // 177-279 Filler
        writer.append(ReportUtils.cropOrPad("", 103));
        //End of Record
    }

    public void writeDataRecords(Appendable writer, Map<Company, ArrayList<MoneyMovementTransaction>> companyToMoneyMovementTransactions, NY_DTF_DTO dto) throws IOException {
        boolean isFirst = true;
        int numOfDataRecordsCreated = 0;
        for (Company company : companyToMoneyMovementTransactions.keySet()) {
            try {
                pspRequestContextManager.setRequestContextCompany(company);
                ArrayList<MoneyMovementTransaction> mmts = companyToMoneyMovementTransactions.get(company);
                MoneyMovementTransaction mmt = mmts.get(0);
                String stateTaxpayerId = prepareStateAgencyId(mmt, 11);

                if (stateTaxpayerId == null) {
                    logger.error("Company " + company.getSourceCompanyId() + " state tax id too long.  The state tax id is \"" +
                            mmt.getAgencyTaxpayerId() + "\".  Skipping output");
                    continue;
                }
                logger.info("Writing NY DTF Data Records for company: " + company.getSourceCompanyId() + " , mmt set size: " + mmts.size());

                SpcfMoney totalPayments = getTotalPayments(mmts);
                //Since PSP is already calculating the total liabilities and creating payments, no need to recalculate it
                // refer to PSP-8548 for more details.
                SpcfMoney totalLiabilities = totalPayments;
                Map<String, SpcfDecimal> lawToLiabilitiesMap = getTotalLiabilitiesLawIdMap(mmts);
                numOfDataRecordsCreated++;
                //Add to total remittance paid
                dto.setTotalRemittancePaid(dto.getTotalRemittancePaid().add(totalPayments));
                if (!isFirst) {
                    writer.append(EOL);
                }
                isFirst = false;
                SpcfCalendar lastPaycheckDate = findLastPaycheckDateInMMTs(mmts);
                if (lastPaycheckDate == null) {
                    logger.error("Last Paycheck Date is null for company " + company + " for frequency of " + mmt.getPaymentFrequency().getPaymentFrequencyId());
                }

                //Start of Record
                //1-11 EIN
                writer.append(stateTaxpayerId);
                //12 Check digit
                writer.append(getCheckDigit(mmt.getAgencyTaxpayerId(), company.getFedTaxId()).toString());
                //13-35 Filler
                writer.append(ReportUtils.cropOrPad("", 23));
                //36-41 Last Payroll Date
                writer.append(lastPaycheckDate.format("MMddyy"));
                //42-44 Zero Fill
                writer.append(ReportUtils.getPaddedWholeNumber(0, 3));
                //45-55 NY SWT Withheld
                SpcfMoney liabilityNYSWT = lawToLiabilitiesMap.containsKey(NY_SWT) ? new SpcfMoney(lawToLiabilitiesMap.get(NY_SWT)) : SpcfMoney.ZERO;
                writer.append(ReportUtils.getPaddedMoney(liabilityNYSWT, 9, 2));
                //56-66 LWT-NYCR & LWT-NYCNR
                // LWT NYCNR is not a part of NY-1MN template, it is NOCALC, confirmed with Linda this is not needed (PSP-8219 for details)
                SpcfMoney liabilityLWTNYCR = lawToLiabilitiesMap.containsKey(LWT_NYCR) ? new SpcfMoney(lawToLiabilitiesMap.get(LWT_NYCR)) : SpcfMoney.ZERO;
                writer.append(ReportUtils.getPaddedMoney(liabilityLWTNYCR, 9, 2));
                //67-77 LWT-YR & LWT-YNR
                SpcfDecimal liabilityLWTYR = lawToLiabilitiesMap.containsKey(LWT_YR) ? lawToLiabilitiesMap.get(LWT_YR) : SpcfMoney.ZERO;
                SpcfDecimal liabilityLWTYNR = lawToLiabilitiesMap.containsKey(LWT_YNR) ? lawToLiabilitiesMap.get(LWT_YNR) : SpcfMoney.ZERO;
                SpcfMoney total_LWT_YR_YNR = new SpcfMoney(liabilityLWTYR.add(liabilityLWTYNR));
                writer.append(ReportUtils.getPaddedMoney(total_LWT_YR_YNR, 9, 2));
                //78-100 Filler
                writer.append(ReportUtils.cropOrPad("", 33));
                //111-121 Amount of credit claimed
                // This is not applicable for us
                writer.append(ReportUtils.getPaddedMoney(SpcfMoney.ZERO, 9, 2));
                //122-132 Total Tax Witheld
                writer.append(ReportUtils.getPaddedMoney(totalLiabilities, 9, 2));
                //133-143 Total Remittance Paid(Total of this payment)
                writer.append(ReportUtils.getPaddedMoney(totalPayments, 9, 2));
                //144-149 Cease Paying Wage Date (N/A for us)
                writer.append(ReportUtils.cropOrPad("", 6));
                //150 Additional Payment Indicator
                writer.append(ADDITIONAL_PAYMENT_INDICATOR);
                //151 Zero Fill
                writer.append(ReportUtils.getPaddedWholeNumber(0, 1));
                //152-279 Filler
                writer.append(ReportUtils.cropOrPad("", 128));
                //End of Record
                //Mark the records as AcknowledgedByAgency and status as executed for this mmt
                markMoneyMovementTransactionsAsProcessed(mmts);
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }
        dto.setNumOfDataRecordsCreated(numOfDataRecordsCreated);
    }

    public void writeHashRecord(Appendable writer, NY_DTF_DTO dto) throws IOException {
        //Start of Record
        //1-4 Hash label identifier
        writer.append(HASH);
        //5 Filler
        writer.append(ReportUtils.cropOrPad("", 1));
        //6-10 Item Type
        writer.append(TOTAL);
        //11 Filler
        writer.append(ReportUtils.cropOrPad("", 1));
        //12-24 Hash amount
        writer.append(ReportUtils.getPaddedMoney(dto.getTotalRemittancePaid(), 11, 2));
        //25-26 Filler
        writer.append(ReportUtils.cropOrPad("", 2));
        //27-32 Hash count
        writer.append(ReportUtils.getPaddedWholeNumber(dto.getNumOfDataRecordsCreated(), 6));
        //33-279 Filler
        writer.append(ReportUtils.cropOrPad("", 247));
        //End of Record
    }

    public void writeTrailerRecord(Appendable writer, NY_DTF_DTO dto) throws IOException {
        //Start of Record
        //1-4 Trailer label record
        writer.append(TRAILER_LABEL_IDENTIFIER);
        //5 Filler
        writer.append(ReportUtils.cropOrPad("", 1));
        //6-12 Number of records (data + hash)
        writer.append(ReportUtils.getPaddedWholeNumber(dto.getNumOfDataRecordsCreated() + 1, 7));
        //13-279 Filler
        writer.append(ReportUtils.cropOrPad("", 267));
        //End of Record
    }

    public void sendAndArchiveFiles() throws S3UploadException,S3ConnectionException {
        //Find files in output directory
        ArrayList<String> createdFilesList = new ArrayList<String>();
        File outputDirectory = new File(OUTPUT_DIRECTORY);
        File[] matchingFiles = outputDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(FILENAME_PREFIX) && name.endsWith(FILE_EXT);
            }
        });

        for (File file : matchingFiles) {
            String filename = OUTPUT_DIRECTORY + File.separator + file.getName();
            logger.info("Adding file to send list - " + filename);
            createdFilesList.add(filename);
        }

        //Create and send email if files were created and recipient list is specified else throw RTE
        String recipient = BatchUtils.getConfigString("psp_batch_state_report_email", "");
        if (StringUtils.isEmpty(recipient)) {
            //If recipient list is empty then throw RuntimeException
            throw new RuntimeException("No recipient email address specified for NY DTF Web Upload, " +
                                               "please correct configuration for psp_batch_state_report_email and rerun " +
                                               "the ACHDebitOffload job");

        } else if (createdFilesList.size() > 0) {
            String subject = "ACH Debit Web Upload Files For " + NY_1MN_PAYMENT;
            StringBuilder message = new StringBuilder();
            message.append("Created ACH Debit web upload files for " + NY_1MN_PAYMENT + "\r\n");
            //todo - Add details of this run like number of companies
            message.append("\r\n");
            message.append("<EOM>");
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"), // server
                                 recipient,                 // to
                                 recipient,                 // from
                                 subject,                   // subject
                                 message.toString(),        // message body
                                 createdFilesList);        // attachments
        }

        //Archive sent files
        String batchJobName = BatchJobType.AchDebitOffload.name();

        for (String filename : createdFilesList) {
            logger.info("Moving file "+filename+" to archive directory "+ARCHIVE_DIRECTORY);
            File unEncFile = new File(filename);
            File file = BatchUtils.encryptFileInStreamsUsingIDPS(unEncFile);
            S3UploadUtils.archive(batchJobName,ARCHIVE_DIRECTORY,file.getAbsolutePath());
        }
    }
    public File encryptFiles(File inpFile){
        File encFile = BatchUtils.encryptFileUsingIDPS(inpFile);
        return encFile;
    }
    /**
     * Gets all ACHDebit MoneyMovementTransactions for a initiation date
     *
     * @param initiationDate initiation date of the payments
     * @return All MoneyMovementTransactions for the initiation date and for a company
     */
    public static HashMap<Company, ArrayList<MoneyMovementTransaction>> getMoneyMovementTransactionsForInitiationDate(PaymentTemplate paymentTemplate, SpcfCalendar initiationDate) {
        // Since HQL doesn't allow substr, additional check and removal are done after fetching
        // can move this to native SQL if performance is not acceptable
        // Validated with Shiva, we just ReadyToSend
        SpcfCalendar startDate = initiationDate.copy();
        CalendarUtils.clearTime(startDate);
        SpcfCalendar endDate = startDate.copy();
        endDate.addDays(1);
        endDate.addMilliseconds(-1);

        logger.info("Trying to find MMTs for criteria startDate = " + startDate.format("yyyyMMddHHmmssS") + " endDate = " + endDate.format("yyyyMMddHHmmssS") +
                            " for payment template = " + paymentTemplate.getPaymentTemplateCd());

        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.InitiationDate().between(startDate, endDate)
                                                                                         .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.ACHDebit))
                                                                                         .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend))
                                                                                         .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                                                                                                                                    .OrderBy(MoneyMovementTransaction.PaymentPeriodBegin(), MoneyMovementTransaction.Company().LegalName())
                                                                                                                                    .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.PaymentFrequency()));

        HashMap<Company, ArrayList<MoneyMovementTransaction>> companyToMoneyMovementTransactions = new
                HashMap<Company, ArrayList<MoneyMovementTransaction>>();

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            Company company = moneyMovementTransaction.getCompany();
            //PSP-12357
            BigDecimal mmtAmount = SpcfUtils.convertToBigDecimal(moneyMovementTransaction.getMoneyMovementTransactionAmount());

            // Check to ensure MMT amount is a positive dollar amount
            if (mmtAmount.compareTo(BigDecimal.ZERO) < 0) {

                // If payment is negative dollar amount, log an error (skip payment and move on to next)
                String msg = String.format("MoneyMovementTransaction amount for company %s:%s is < $0.00 " +
                                "(MMT id: %s, MMT amount: $%,.2f).",
                        company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        moneyMovementTransaction.getId(),
                        mmtAmount);
                logger.error(msg+" Skipping AchDebitOffload of Negative MMT.");
                continue;
            }

            if (companyToMoneyMovementTransactions.containsKey(company)) {
                companyToMoneyMovementTransactions.get(company).add(moneyMovementTransaction);
            } else {
                ArrayList<MoneyMovementTransaction> moneyMovementTransactionList = new ArrayList<MoneyMovementTransaction>();
                moneyMovementTransactionList.add(moneyMovementTransaction);
                companyToMoneyMovementTransactions.put(company, moneyMovementTransactionList);
            }
        }

        return companyToMoneyMovementTransactions;
    }

    /**
     * Gets the liabilities map for all MoneyMovementTransaction passed in for laws
     * 1. We do not need to ignore applied ATDs
     * 2. Negative amounts for laws need to reported as Zero AND
     * 3. Negatives need to be adjust with other laws in the template
     *
     * @param moneyMovementTransactions The MoneyMovementTransactions to total
     * @return The total liabilities for all MoneyMovementTransaction passed in
     */
    public Map<String, SpcfDecimal> getTotalLiabilitiesLawIdMap(ArrayList<MoneyMovementTransaction> moneyMovementTransactions) {
        //TreeMap to keep it sorted on law ids
        Map<String, SpcfDecimal> liabilitiesMap = new TreeMap<String, SpcfDecimal>();
        Boolean needsNegativeAdjustmentAcrossLaws = Boolean.FALSE;

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            DomainEntitySet<FinancialTransaction> financialTransactions = moneyMovementTransaction.getFinancialTransactionCollection()
                                                                                                  .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created,
                                                                                                                                                                               TransactionStateCode.Executed,
                                                                                                                                                                               TransactionStateCode.Completed));

            for (FinancialTransaction financialTransaction : financialTransactions) {

                Law law = financialTransaction.getLaw();
                if (law == null) {
                    logger.warn("Law is null for FT " + financialTransaction.getId() + " amount " + financialTransaction.getFinancialTransactionAmount() +
                                        " for company " + financialTransaction.getCompany().getSourceCompanyId());
                    continue;
                }
                String lawId = law.getLawId();
                if (StringUtils.isNotEmpty(lawId)) {
                    SpcfDecimal total = liabilitiesMap.containsKey(lawId) ? liabilitiesMap.get(lawId) : SpcfMoney.ZERO;
                    if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                        total = total.add(financialTransaction.getFinancialTransactionAmount());
                    } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                        total = total.subtract(financialTransaction.getFinancialTransactionAmount());
                    }
                    if (!needsNegativeAdjustmentAcrossLaws && total.isLessThanEqualTo(SpcfMoney.ZERO)) {
                        needsNegativeAdjustmentAcrossLaws = Boolean.TRUE;
                    }
                    liabilitiesMap.put(lawId, total);
                }
            }
        }

        if (needsNegativeAdjustmentAcrossLaws) {
            adjustLiabilitiesAcrossLawsForNegativeAmount(liabilitiesMap);
        }

        return liabilitiesMap;
    }

    public void adjustLiabilitiesAcrossLawsForNegativeAmount(Map<String, SpcfDecimal> liabilitiesMap) {
        //if any law is having a negative amount it needs to be adjusted against other laws
        for (Map.Entry<String, SpcfDecimal> entry : liabilitiesMap.entrySet()) {
            //find law with negative amount
            if (entry.getValue() != null && entry.getValue().isLessThan(SpcfMoney.ZERO)) {
                //get law id
                String negativeLawIdString = entry.getKey();
                //get abs amount to be adjusted(with negative sign)
                SpcfDecimal absNegativeAmount = entry.getValue().abs();
                //Iterate through Map to find laws we can reduce
                for (Map.Entry<String, SpcfDecimal> findEntry : liabilitiesMap.entrySet()) {
                    //Make sure it is not the same law as we are trying to adjust, since we are checking for a non-negat
                    //-ve value this wont happen and we need to find a law with a positive value
                    if (findEntry.getValue() != null && findEntry.getValue().isGreaterThan(SpcfMoney.ZERO)) {
                        //found law has an amount >= negative amount
                        if (findEntry.getValue().isGreaterThanEqualTo(absNegativeAmount)) {
                            //reduce the amount on the law found by the negative amount
                            findEntry.setValue(findEntry.getValue().subtract(absNegativeAmount));
                            //negative amount now becomes zero because all of it is exhausted
                            absNegativeAmount = SpcfMoney.ZERO;
                            //We do not need to continue because negative amount is already adjusted
                            break;
                        } else {
                            //only a part of negative amount can be adjusted but the law amount becomes zero
                            absNegativeAmount = absNegativeAmount.subtract(findEntry.getValue());
                            //all of the law due amount is exhausted
                            findEntry.setValue(SpcfMoney.ZERO);
                        }

                    }
                }
                //negative amount for the current law should have been exhausted so make the correction
                //Check if the negative amount for the law is exhausted or not
                if (absNegativeAmount.isGreaterThan(SpcfMoney.ZERO)) {
                    logger.warn("Negative MMT encountered, ACHDebit will report zero - Law " + negativeLawIdString + " & negative amount remaining " + absNegativeAmount);
                }
                liabilitiesMap.put(negativeLawIdString, SpcfMoney.ZERO);
            }
        }
    }

    public SpcfCalendar findLastPaycheckDateInMMTs(List<MoneyMovementTransaction> mmtList) {
        SpcfCalendar lastPayrollDate = null;
        for (MoneyMovementTransaction mmt : mmtList) {
            for (FinancialTransaction financialTransaction : mmt.getFinancialTransactionCollection()) {
                PayrollRun payrollRun = financialTransaction.getPayrollRun();
                if (payrollRun != null) {
                    SpcfCalendar paycheckDate = payrollRun.getPaycheckDate();
                    if (lastPayrollDate == null || paycheckDate.after(lastPayrollDate)) {
                        lastPayrollDate = paycheckDate;
                    }
                }
            }
        }
        return lastPayrollDate;

    }

    /**
     * Marks the MMTs as Acknowledged & Executed
     *
     * @param moneyMovementTransactions
     */
    public void markMoneyMovementTransactionsAsProcessed(ArrayList<MoneyMovementTransaction> moneyMovementTransactions) {
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency);
            moneyMovementTransaction.setStatus(PaymentStatus.Executed);
            TransactionState executedState = TransactionState.findTransactionState(TransactionStateCode.Executed);
            for (FinancialTransaction ft : moneyMovementTransaction.getFinancialTransactionCollection()) {
                ft.addTaxPaymentTransactionState(executedState);
            }
            moneyMovementTransaction.setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());
        }
    }

    /**
     * @param pStateEIN
     * @param fedEIN
     * @return
     */
    public static Integer getCheckDigit(String pStateEIN, String fedEIN) {
        int startValueForA = 10;    //As per specification A=10, B=11, C=12, D=13...etc   as given in document NY Checkdigitpub83.pdf (page 7) PSP-8219 jira
        int blankSpaceValue = 0;
        int LENGTH_AGENCY_ID = 11;

        if (pStateEIN == null || pStateEIN.trim().equals("")) {
            logger.warn("Error in calculating check digit for stateEIN:" + pStateEIN);
            return 0;
        }
        boolean isValidAgencyId = isValidAgencyId(pStateEIN, fedEIN);

        //get last digit of a valid agencyid as check digit  -- as per Linda
        if (isValidAgencyId && " ".equals(pStateEIN.substring(pStateEIN.length() - 2, pStateEIN.length() - 1))) {
            try {
                return Integer.parseInt(pStateEIN.substring(pStateEIN.length() - 1));
            } catch (Exception ex) {
                //Continue. Not do any thing here
            }
        }
        if (pStateEIN.length() > LENGTH_AGENCY_ID) {
            // pStateEIN =  pStateEIN.replaceAll("[^a-zA-Z\\d\\s:]",""); // replace all non alphanumeric with empty
            pStateEIN = pStateEIN.replaceAll("\\W", ""); // replace all non alphanumeric with empty
            pStateEIN = pStateEIN.length() > LENGTH_AGENCY_ID ? pStateEIN.substring(0, 11) : pStateEIN;
        }

        int accountNumberWeight = 0;
        //Multiply each of the 11-character positions by an assigned weight of 1 through 11.Add the products of each multiplication
        //divide the result by nine. Subtract the remainder from 9; the difference is the check digit. Blank spaces equate to zero (0).
        for (int i = 0; i < pStateEIN.length(); i++) {
            int multiplier = i + 1;
            int value = 0;
            char character = pStateEIN.subSequence(i, i + 1).charAt(0);
            if ((character >= 'A' && character <= 'Z')) {
                value = character - 'A' + startValueForA;
            } else if (character == ' ') {
                value = blankSpaceValue;
            } else if ((character >= '0' && character <= '9')) {
                value = Integer.parseInt(pStateEIN.subSequence(i, i + 1).toString());
            }
            accountNumberWeight += (value * multiplier);
        }
        if (accountNumberWeight == 0) {
            return accountNumberWeight;
        } else {
            return 9 - accountNumberWeight % 9;
        }

    }

    /**
     * @param agencyId
     * @param fedEIN
     * @return
     */
    private static boolean isValidAgencyId(String agencyId, String fedEIN) {
        boolean isValid = false;
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_WH);
        for (PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : paymentTemplate.getPaymentTemplatePaymentMethods()
                                                                                        .sort(PaymentTemplatePaymentMethod.PaymentMethod())) {
            for (PaymentMethodRequirement paymentMethodRequirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
                if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                    AgencyIdRequirement agencyIdRequirement = (AgencyIdRequirement) paymentMethodRequirement;

                    if (agencyIdRequirement.getRequired() && StringUtils.isNotEmpty(agencyIdRequirement.getPattern())) {
                        boolean meetsRequirements = agencyIdRequirement.matchesPattern(agencyId) &&
                                agencyIdRequirement.meetsCustomRequirements(agencyId, fedEIN);
                        boolean meetsDefaultIdChecks = !agencyIdRequirement.getProhibitDefaultIds() || agencyIdRequirement.isNotADefaultId(agencyId, fedEIN);
                        if (meetsRequirements && meetsDefaultIdChecks) {
                            isValid = true;
                            break;
                        }
                    }

                }
            }
        }
        return isValid;
    }

    public String prepareStateAgencyId(MoneyMovementTransaction moneyMovementTransaction, int maximumSize) {
        String stateAgencyId = moneyMovementTransaction.getAgencyTaxpayerId();

        if (stateAgencyId == null) {
            stateAgencyId = "";
        }

        //When pattern is 9/11 digit followed by space and digit
        Pattern aidPattern = Pattern.compile("^\\d{9}(\\d\\d)?\\s\\d$");
        Matcher matcher = aidPattern.matcher(stateAgencyId);
        if (matcher.find()) {
            //remove the check digit and send the agency id
            stateAgencyId = stateAgencyId.substring(0, stateAgencyId.length() - 2);
        } else {
            //This means that agency id does not have a check digit
            // Remove all except for letters and numbers from state agency id
            stateAgencyId = stateAgencyId.replaceAll("\\W", "");
            if (stateAgencyId.length() > maximumSize) {
                //Resize to maximum of 11 digits
                stateAgencyId = stateAgencyId.substring(0, 11);
            }
        }

        return ReportUtils.cropOrPad(stateAgencyId, maximumSize);
    }

    /**
     * Gets the total payments for all MoneyMovementTransaction passed in
     *
     * @param moneyMovementTransactions The MoneyMovementTransactions to total
     * @return The total payments for all MoneyMovementTransaction passed in
     */
    public SpcfMoney getTotalPayments(ArrayList<MoneyMovementTransaction> moneyMovementTransactions) {
        SpcfMoney total = SpcfMoney.ZERO;

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            total = (SpcfMoney) total.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());
        }

        return total;
    }

    protected void writeData(FileWriter pFileWriter, String pData) throws IOException {
        if (pData == null) {
            pData = "";
        }
        pFileWriter.write(pData);
    }

}
