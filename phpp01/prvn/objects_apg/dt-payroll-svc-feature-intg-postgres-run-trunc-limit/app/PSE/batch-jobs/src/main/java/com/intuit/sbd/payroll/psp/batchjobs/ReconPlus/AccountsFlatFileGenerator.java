package com.intuit.sbd.payroll.psp.batchjobs.ReconPlus;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.printedcheckfile.ReconPlusFile;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpCommonEncryptedWriter;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * User: mwaqarbaig
 * Date: Aug 11, 2011
 * Time: 2:21:08 PM
 */
public class AccountsFlatFileGenerator {
    private static SpcfLogger logger = Application.getLogger(AccountsFlatFileGenerator.class);

    public static final String TAX_ACCOUNTS_FILE_PREFIX = "Recon_Plus_Tax_Accounts_";
    public static final String RETURNS_FILE_PREFIX = "Recon_Plus_Returns_";
    public static final String FILE_EXTENSION = ".txt";

    public static void createACHAccountsFile(AccountingReportFileType pFileType, SpcfCalendar pRunDate) throws Exception {
        if (pRunDate == null) {
            pRunDate = PSPDate.getPSPTime().copy();
        }
        CalendarUtils.clearTime(pRunDate);
        logger.info("createAccountsFile [" + pFileType + "] started");
        StopWatch timer = StopWatch.startTimer();

        String outputDirectory = BatchUtils.getConfigString("psp_batch_ftp_send_dir");
        String fileName = outputDirectory + File.separator;

        switch (pFileType) {
            case TaxAccountsReconPlus:
                fileName += TAX_ACCOUNTS_FILE_PREFIX;
                break;
            case ReturnsAccountsReconPlus:
                fileName += RETURNS_FILE_PREFIX;
                break;
        }
        fileName += StringFormatter.formatDate(pRunDate, "yyyyMMddHHmm") + FILE_EXTENSION;

        switch (pFileType) {
            case TaxAccountsReconPlus:
                createTaxAccountsFile(fileName, pRunDate);
                break;
            case ReturnsAccountsReconPlus:
                createReturnsAccountsFile(fileName, pRunDate);
                break;
        }
        logger.info("createFile [" + pFileType + "] finished in " + timer.getElapsedTimeString());
    }

    private static void createTaxAccountsFile(String pFileName, SpcfCalendar pRunDate) throws Exception {
        PgpWriter fileWriter = null;
        ScrollableResults detailRecords = null;
        ScrollableResults debitRecords = null;
        ScrollableResults mmtsToInclude = null;
        StopWatch timer = StopWatch.startTimer();

        try {
            PSPRequestContextManager pspRequestContextManager = PSPRequestContextManagerHelper.getPSPRequestContextManager();
            /*  Get the PIBA Intuit Account */
            IntuitBankAccount taxBankAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_TAX);
            if (taxBankAccount == null) {
                throw new RuntimeException("Intuit Tax Bank account not set");
            }


            if (!taxBankAccount.equals(IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.AgencyTaxCredit, CreditDebitCode.Debit))) {
                /*  see note below  */
                logger.error("Intuit Tax Bank Account is not the same for AgencyTaxCredit anymore.");
                throw new RuntimeException("Intuit Tax Bank Account is not the same for AgencyTaxCredit anymore.");
            }

            // ACH tax payments
            if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
                detailRecords = EntryDetailRecord.findEDRsWithMMTsForIntuitBankAccountCriteria(taxBankAccount, pRunDate);
            } else {
                detailRecords = EntryDetailRecord.findEDRsWithMMTsForIntuitBankAccount(taxBankAccount, pRunDate);
            }

            // ACH debits
            debitRecords = FinancialTransaction.findFTsWithMMTsForIntuitBankAccount(taxBankAccount, pRunDate);

            /*  Note:
            *   We're getting the EFTPS transactions from the MMT only and not consulting the FTs.
            *   This is being done to avoid the performance hit that we'd see if we do a MMT<-join->FT lookup.
            *   Assumption: An EFTPS always goes to Intuit Tax Bank account so no need to consult with FT.
            *   The check above will break if this changes in the future  */
            mmtsToInclude = Application.findScrollable(MoneyMovementTransaction.class, new com.intuit.sbd.payroll.psp.query.Query<MoneyMovementTransaction>()
                    .Where(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.EFTPS, PaymentMethod.EDI, PaymentMethod.ACHDebit)
                    .And(MoneyMovementTransaction.InitiationDate().equalTo(pRunDate))
                    .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Executed))
                    .And(MoneyMovementTransaction.TaxPaymentStatus().notEqualTo(TaxPaymentStatus.RejectedByAgency)))
                    .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.Company().QuickbooksInfo()));

            Boolean edrRecordsExist = detailRecords.next();
            Boolean debitRecordsExist = debitRecords.next();
            Boolean mmtRecordsExist = mmtsToInclude.next();

            // always produce a file (empty if need be)
            AccountingReportFile reportFile = new AccountingReportFile();
            reportFile.setStatus(AccountingReportFileStatus.Created);
            reportFile.setType(AccountingReportFileType.TaxAccountsReconPlus);
            fileWriter = new PgpCommonEncryptedWriter(BatchUtils.getTfaPgpKeys());
            fileWriter.open(pFileName);
            reportFile.setFileName(((PgpCommonEncryptedWriter) fileWriter).getEncryptedFileName());
            Application.save(reportFile);
            if (edrRecordsExist || mmtRecordsExist || debitRecordsExist) {
                logger.info(String.format("Processing transactions for Tax ReconPlus."));
                logger.info("Creating tax accounts file: " + pFileName);
                ReconPlusFile reconPlusFile = new ReconPlusFile();
                int successfullyWritten = 0;

                String accountNumber = taxBankAccount.getBankAccount().getAccountNumber();
                if (edrRecordsExist) {
                    do {
                        try {
                            EntryDetailRecord detailRecord = (EntryDetailRecord) detailRecords.get(0);
                            pspRequestContextManager.setRequestContextCompany(detailRecord.getCompany());

                            if(detailRecord.getAmount() != null && detailRecord.getAmount().isGreaterThan(SpcfMoney.ZERO)) {
                                processAndWriteTaxAccountMMT(reconPlusFile, fileWriter, accountNumber, detailRecord.getCreditDebitIndicator(), detailRecord.getMoneyMovementTransaction(), null);
                                successfullyWritten++;
                            }

                            if (successfullyWritten % 500 == 0) {
                                logger.info("working -- completed processing " + successfullyWritten + " entry detail records " + timer.getElapsedTimeString());
                            }

                            // Keep cache clean
                            //Application.evict(detailRecord.getMoneyMovementTransaction().getQbdtTransactionInfo());
                            Application.evict(detailRecord.getMoneyMovementTransaction());
                            Application.evict(detailRecord.getCompany());
                            Application.evict(detailRecord);
                        } finally {
                            pspRequestContextManager.clearRequestContextCompany();
                        }

                    } while (detailRecords.next());
                }
                logger.info("Finished processing edr records");

                if (debitRecordsExist) {
                    do {
                        FinancialTransaction debitTransaction = (FinancialTransaction) debitRecords.get(0);
                        // skip $0.00
                        if(debitTransaction.getFinancialTransactionAmount() != null && debitTransaction.getFinancialTransactionAmount().isGreaterThan(SpcfMoney.ZERO)) {
                            processAndWriteTaxAccountMMT(reconPlusFile, fileWriter, accountNumber, CreditDebitCode.Credit, debitTransaction.getMoneyMovementTransaction(), debitTransaction);
                            successfullyWritten++;
                        }

                        if (successfullyWritten % 500 == 0) {
                            logger.info("working -- completed processing " + successfullyWritten + " debit records " + timer.getElapsedTimeString());
                        }

                        // Keep cache clean
                        Application.evict(debitTransaction.getMoneyMovementTransaction());
                        Application.evict(debitTransaction.getCompany());
                        Application.evict(debitTransaction);
                    } while (debitRecords.next());
                }
                logger.info("Finished processing debit records");

                if (mmtRecordsExist) {
                    int edrSuccesfullyWritten = successfullyWritten;
                    do {
                        MoneyMovementTransaction moneyMovementTransaction = (MoneyMovementTransaction) mmtsToInclude.get(0);

                        if(moneyMovementTransaction.getMoneyMovementTransactionAmount() != null && moneyMovementTransaction.getMoneyMovementTransactionAmount().isGreaterThan(SpcfMoney.ZERO)) {
                            processAndWriteTaxAccountMMT(reconPlusFile, fileWriter, accountNumber, CreditDebitCode.Debit, moneyMovementTransaction, null);
                            successfullyWritten++;
                        }

                        if ((successfullyWritten - edrSuccesfullyWritten) % 500 == 0) {
                            logger.info("working -- completed processing " + (successfullyWritten - edrSuccesfullyWritten) + " money movement transactions " + timer.getElapsedTimeString());
                        }

                        // Keep cache clean
                        Application.evict(moneyMovementTransaction.getCompany());
                        Application.evict(moneyMovementTransaction);
                    } while (mmtsToInclude.next());
                }

                logger.info(String.format("Successfully wrote %d record(s)", successfullyWritten));
            }
        }
        finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
            if (detailRecords != null) {
                detailRecords.close();
            }
            if (debitRecords != null) {
                debitRecords.close();
            }
            if (mmtsToInclude != null) {
                mmtsToInclude.close();
            }
        }
    }

    private static void processAndWriteTaxAccountMMT(ReconPlusFile pReconPlusFile, PgpWriter pFileWriter, String pAccountNumber, CreditDebitCode creditDebitIndicator, MoneyMovementTransaction pMMT, FinancialTransaction pFinancialTransaction) throws IOException {
        int year = 0;
        if (pMMT.getPaymentPeriodEnd() != null) {
            year = pMMT.getPaymentPeriodEnd().getYear();
        }
        int quarter = 0;
        if (pMMT.getPaymentPeriodEnd() != null) {
            quarter = CalendarUtils.getQuarterAsInt(pMMT.getPaymentPeriodEnd());
        }

        String offloadBatch_ParentFile_FK = "";
        String paymentDetails = "";
        SpcfMoney transactionAmount = pMMT.getMoneyMovementTransactionAmount();
        switch (pMMT.getMoneyMovementPaymentMethod()) {
            case EFTPS:
                EftpsPaymentDetail eftpsPaymentDetail = EftpsPaymentDetail.findPaymentDetailByMoneyMovementTransaction(pMMT);
                if (eftpsPaymentDetail != null) {
                    paymentDetails = eftpsPaymentDetail.getPaymentDetails();
                    EftpsFile eftpsFile = eftpsPaymentDetail.getParentFile();
                    if (eftpsFile != null && eftpsFile.getId() != null) {
                        offloadBatch_ParentFile_FK = eftpsFile.getId().toString();
                    }
                    Application.evict(eftpsPaymentDetail);
                }
                break;
            case EDI:
                EdiPaymentDetail ediPaymentDetail = EdiPaymentDetail.findPaymentDetailByMoneyMovementTransaction(pMMT);
                if (ediPaymentDetail != null) {
                    paymentDetails = ediPaymentDetail.getPaymentDetails();
                    StateEdiTaxFile stateEdiTaxFile = ediPaymentDetail.getParentFile();
                    if (stateEdiTaxFile != null && stateEdiTaxFile.getId() != null) {
                        offloadBatch_ParentFile_FK = stateEdiTaxFile.getId().toString();
                    }
                    Application.evict(ediPaymentDetail);
                }
                break;
            case ACHDirectDeposit:
                if (pMMT.getOffloadBatch() != null) {
                    offloadBatch_ParentFile_FK = pMMT.getOffloadBatch().getId().toString();
                }
                transactionAmount = pFinancialTransaction.getFinancialTransactionAmount();
                break;
            default:
                if (pMMT.getOffloadBatch() != null) {
                    offloadBatch_ParentFile_FK = pMMT.getOffloadBatch().getId().toString();
                }
                break;
        }

        String agencyTaxPayerId = "";
        if (pMMT.getAgencyTaxpayerId() != null) {
            agencyTaxPayerId = pMMT.getAgencyTaxpayerId();
        }

        String paymentTemplateCd = "";
        if (pMMT.getPaymentTemplate() != null) {
            paymentTemplateCd = pMMT.getPaymentTemplate().getPaymentTemplateCd();
        }

        pReconPlusFile.writeTaxAccountsDetailRecord(pFileWriter,
                pAccountNumber,
                transactionAmount,
                pMMT.getInitiationDate(),
                null,/*   no settlement date    */
                pMMT.getMoneyMovementPaymentMethod().toString(),
                agencyTaxPayerId,
                pMMT.getCompany().getLegalName(),
                pMMT.getCompany().getSourceCompanyId(),
                paymentTemplateCd,
                quarter, year,
                pMMT.getCreatorId(),
                pMMT.getCompany().getFedTaxId(),
                offloadBatch_ParentFile_FK,
                pMMT.getStatus().toString(),
                paymentDetails, creditDebitIndicator);
    }

    private static void createReturnsAccountsFile(String pFileName, SpcfCalendar pRunDate) throws Exception {
        FileWriter fileWriter = null;
        ScrollableResults entryDetailRecords = null;
        ScrollableResults debitRecords = null;
        ScrollableResults mmtsToInclude = null;
        StopWatch timer = StopWatch.startTimer();
        try {
            /*  Get the entries for the returns account  */
            IntuitBankAccount returnsBankAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_ER_RETURN);
            if (returnsBankAccount == null) {
                throw new RuntimeException("Intuit ER Returns Bank account not set");
            }

            SpcfCalendar startDate= pRunDate.copy();
            CalendarUtils.clearTime(startDate);
            SpcfCalendar endDate = startDate.copy();
            endDate.addDays(1);
            endDate.addMilliseconds(-1);

            // ACH tax payments
            entryDetailRecords = EntryDetailRecord.findEDRsWithMMTsForIntuitBankAccount(returnsBankAccount, pRunDate);
            // ACH debits
            debitRecords = FinancialTransaction.findFTsWithMMTsForIntuitBankAccount(returnsBankAccount, pRunDate);

            /*  Now get the entries for the txn returns received today  */
            String hqlSelect = "SELECT mmt FROM com.intuit.sbd.payroll.psp.domain.TransactionReturn tr JOIN tr.MoneyMovementTransaction mmt \n" +
                    " WHERE tr.ReturnStatusEffectiveDate BETWEEN :startDate AND :endDate";
            Query hibernateQuery = Application.createHibernateQuery(hqlSelect);
            hibernateQuery.setReadOnly(true);
            hibernateQuery.setParameter("startDate", startDate);
            hibernateQuery.setParameter("endDate", endDate);

            mmtsToInclude = hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
            Boolean edrRecordsExist = entryDetailRecords.next();
            Boolean debitRecordsExist = debitRecords.next();
            Boolean mmtRecordsExist = mmtsToInclude.next();
            if (!edrRecordsExist && !mmtRecordsExist && !debitRecordsExist) {
                logger.info("No returns to process");
            }
            else {
                logger.info(String.format("Processing transactions for Returns ReconPlus."));
                logger.info("Creating accounts file: " + pFileName);
                int successfullyWritten = 0;
                ReconPlusFile reconPlusFile = new ReconPlusFile();
                fileWriter = new FileWriter(pFileName);
                AccountingReportFile reportFile = new AccountingReportFile();
                reportFile.setFileName(pFileName);
                reportFile.setStatus(AccountingReportFileStatus.New);
                reportFile.setType(AccountingReportFileType.ReturnsAccountsReconPlus);
                Application.save(reportFile);
                String accountNumber = returnsBankAccount.getBankAccount().getAccountNumber();

                if (edrRecordsExist) {
                    do {
                        EntryDetailRecord entryDetailRecord = (EntryDetailRecord) entryDetailRecords.get(0);
                        processAndWriteReturnsAccountMMT(reconPlusFile, fileWriter, accountNumber, entryDetailRecord.getMoneyMovementTransaction(), null);
                        successfullyWritten++;

                        if (successfullyWritten % 500 == 0) {
                            logger.info("working -- completed processing " + successfullyWritten + " entry detail records " + timer.getElapsedTimeString());
                        }

                        // Keep cache clean
                        //Application.evict(entryDetailRecord.getMoneyMovementTransaction().getQbdtTransactionInfo());
                        Application.evict(entryDetailRecord.getMoneyMovementTransaction());
                        Application.evict(entryDetailRecord.getCompany());
                        Application.evict(entryDetailRecord);
                    } while (entryDetailRecords.next());
                }

                if (debitRecordsExist) {
                    do {
                        FinancialTransaction debitRecord = (FinancialTransaction) debitRecords.get(0);
                        if(debitRecord.getFinancialTransactionAmount() != null && debitRecord.getFinancialTransactionAmount().isGreaterThan(SpcfMoney.ZERO)) {
                            processAndWriteReturnsAccountMMT(reconPlusFile, fileWriter, accountNumber, debitRecord.getMoneyMovementTransaction(), debitRecord);
                            successfullyWritten++;
                        }

                        if (successfullyWritten % 500 == 0) {
                            logger.info("working -- completed processing " + successfullyWritten + " debit records " + timer.getElapsedTimeString());
                        }

                        // Keep cache clean
                        Application.evict(debitRecord.getMoneyMovementTransaction());
                        Application.evict(debitRecord.getCompany());
                        Application.evict(debitRecord);
                    } while (debitRecords.next());
                }

                if (mmtRecordsExist) {
                    int edrSuccesfullyWritten = successfullyWritten;
                    do {
                        MoneyMovementTransaction transaction = (MoneyMovementTransaction) mmtsToInclude.get(0);
                        processAndWriteReturnsAccountMMT(reconPlusFile, fileWriter, accountNumber, transaction, null);
                        successfullyWritten++;

                        if ((successfullyWritten - edrSuccesfullyWritten) % 500 == 0) {
                            logger.info("working -- completed processing " + (successfullyWritten - edrSuccesfullyWritten) + " money movement transactions " + timer.getElapsedTimeString());
                        }

                        // Keep cache clean
                        Application.evict(transaction.getCompany());
                        Application.evict(transaction);
                    } while (mmtsToInclude.next());
                }
                
                reportFile.setFileName(pFileName);
                reportFile.setStatus(AccountingReportFileStatus.Created);
                Application.save(reportFile);
                logger.info(String.format("Successfully wrote %d record(s)", successfullyWritten));
            }
        }
        finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
            if (entryDetailRecords != null) {
                entryDetailRecords.close();
            }
            if (debitRecords != null) {
                debitRecords.close();
            }
            if (mmtsToInclude != null) {
                mmtsToInclude.close();
            }
        }
    }

    private static void processAndWriteReturnsAccountMMT(ReconPlusFile pReconPlusFile, FileWriter pFileWriter, String pBankAccountNumber, MoneyMovementTransaction pMMT, FinancialTransaction pFinancialTransaction) throws IOException {
        SpcfMoney transactionAmount = pMMT.getMoneyMovementTransactionAmount();
        if(pFinancialTransaction != null) {
            transactionAmount = pFinancialTransaction.getFinancialTransactionAmount();
        }
        pReconPlusFile.writeReturnsAccountsDetailRecord(pFileWriter,
                pBankAccountNumber,
                transactionAmount,
                pMMT.getInitiationDate(),
                pMMT.getDueDate(),
                pMMT.getMoneyMovementPaymentMethod().toString(),
                " ",
                " ",
                pMMT.getCompany().getLegalName(),
                pMMT.getCompany().getSourceCompanyId(),
                pMMT.getCreatorId(),
                pMMT.getCompany().getFedTaxId(),
                " ");
    }
}
