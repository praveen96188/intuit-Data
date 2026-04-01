package com.intuit.sbd.payroll.psp.batchjobs.printedchecks;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.printedcheckfile.PositivePayFile;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.printedcheckfile.ReconPlusFile;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriterFactory;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpCommonEncryptedWriter;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileSourceCode;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 5, 2011
 * Time: 8:00:26 AM
 */
public class PrintedCheckFlatFileGenerator {
    private static SpcfLogger logger = Application.getLogger(PrintedCheckFlatFileGenerator.class);

    public static final String POSITIVE_PAY_FILE_PREFIX = "Positive_Pay_";
    public static final String RECON_PLUS_FILE_PREFIX = "Recon_Plus_";
    public static final String ENCRYPTED_FILE_EXTENSION = ".pgp";
    public static final String UNENCRYPTED_FILE_EXTENSION = ".txt";

    public static void createFile(AccountingReportFileType pFileType) throws Exception {
        logger.info("createFile started for AccountingReportFileType:" + pFileType);
        StopWatch timer = StopWatch.startTimer();

        boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);

        String outputDirectory = BatchUtils.getConfigString("psp_batch_ftp_send_dir");

        String fileName = outputDirectory + File.separator;

        String fileExt = UNENCRYPTED_FILE_EXTENSION;
        switch (pFileType) {
            case PositivePay:
                fileName += POSITIVE_PAY_FILE_PREFIX;
                fileExt = enableEncryption ? ENCRYPTED_FILE_EXTENSION : UNENCRYPTED_FILE_EXTENSION;
                break;
            case PrintedCheckReconPlus:
                fileName += RECON_PLUS_FILE_PREFIX;
                break;
        }

        fileName += StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + fileExt;

        DomainEntitySet<AccountingReportFile> printedCheckFiles = AccountingReportFile.findByTypeAndStatus(pFileType, AccountingReportFileStatus.New, true);


        switch (pFileType) {
            case PositivePay:
                logger.info("Creating check file: " + fileName);
                createPositivePayFile(printedCheckFiles, fileName);
                break;
            case PrintedCheckReconPlus:
                if (printedCheckFiles.isEmpty()) {
                    // create an empty file
                    AccountingReportFile reconPlusFile = new AccountingReportFile();
                    reconPlusFile.setType(AccountingReportFileType.PrintedCheckReconPlus);
                    reconPlusFile.setStatus(AccountingReportFileStatus.New);
                    Application.save(reconPlusFile);
                    printedCheckFiles.add(reconPlusFile);
                }
                fileName = createReconPlusFile(printedCheckFiles, fileName);
                logger.info("Creating check file: " + fileName);
                break;
        }

        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            printedCheckFile.setStatus(AccountingReportFileStatus.Created);
            printedCheckFile.setFileName(fileName);
            Application.save(printedCheckFile);
        }

        File outputFile = new File(fileName);

        logger.info("createFile for AccountingReportFileType " + pFileType + " finished in " + timer.getElapsedTimeString() + "(with " + printedCheckFiles.size() +
                "checks)  File Exists:" + outputFile.exists() + " File Size:" + outputFile.length());
    }

    private static void createPositivePayFile(DomainEntitySet<AccountingReportFile> pPrintedCheckFiles, String pFileName) throws Exception {
        PgpWriter writer = null;
        String positiveFileAccountNumber = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_CHECK).getBankAccount().getAccountNumber();

        try {
            PositivePayFile positivePayFile = new PositivePayFile();
            AccountingReportFile printedCheckFileForVoids = null;
            for (AccountingReportFile printedCheckFile : pPrintedCheckFiles) {
                if(printedCheckFileForVoids == null) {
                    printedCheckFileForVoids = printedCheckFile;
                }
                for (CheckPrintBatch checkPrintBatch : printedCheckFile.getPositivePayFileBatchesCollection()) {
                    List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch((AgencyCheckBatch)checkPrintBatch, true);
                    if (checkPrintBatch instanceof AgencyCheckBatch && ((AgencyCheckBatch)checkPrintBatch).getSuperCheck()) {
                        SpcfDecimal amount = SpcfMoney.ZERO;
                        for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                            MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                            amount = amount.add(payment.getMoneyMovementTransactionAmount());

                        }
                        //one line per SuperCheck, but many details are on the payments themselves.  Always same so get first as representative.
                        MoneyMovementTransaction representativePayment = paymentBatchAssociations.get(0).getMoneyMovementTransaction();
                        PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo =
                                    PaymentTemplatePrintedCheckInfo.findPaymentTemplatePrintedCheckInfo(representativePayment.getPaymentTemplate());
                        positivePayFile.addDetailRecord(representativePayment.findIntuitDebitAccount().getAccountNumber(),
                                                        representativePayment.getReferenceNumber(),
                                                        new SpcfMoney(amount),
                                                        representativePayment.getSettlementDate(),
                                                        paymentTemplatePrintedCheckInfo.getNameLine1(),
                                                        paymentTemplatePrintedCheckInfo.getNameLine2(),
                                                        false);


                    } else {
                        for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                            MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                            PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo =
                                    PaymentTemplatePrintedCheckInfo.findPaymentTemplatePrintedCheckInfo(payment.getPaymentTemplate());
                            positivePayFile.addDetailRecord(payment.findIntuitDebitAccount().getAccountNumber(),
                                                            payment.getReferenceNumber(),
                                                            payment.getMoneyMovementTransactionAmount(),
                                                            payment.getSettlementDate(),
                                                            paymentTemplatePrintedCheckInfo.getNameLine1(),
                                                            paymentTemplatePrintedCheckInfo.getNameLine2(),
                                                            false);
                        }
                    }
                }
            }

            // add voided checks
            DomainEntitySet<VoidedCheck> voidedChecks;
            if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
                Expression<VoidedCheck> query = new Query<VoidedCheck>()
                        .Where(VoidedCheck.AccountingReportFile().isNull())
                        .EagerLoad(VoidedCheck.AgencyCheckBatch().PaymentBatchAssocSet().Filter().Company().equalTo(VoidedCheck.Company()))
                        .EagerLoad(VoidedCheck.AgencyCheckBatch());
                voidedChecks = Application.find(VoidedCheck.class, query);
            } else {
                voidedChecks = Application.find(VoidedCheck.class, VoidedCheck.AccountingReportFile().isNull());
            }

            if(printedCheckFileForVoids == null && voidedChecks.size() > 0) {
                printedCheckFileForVoids = new AccountingReportFile();
                printedCheckFileForVoids.setStatus(AccountingReportFileStatus.New);
                printedCheckFileForVoids.setType(AccountingReportFileType.PositivePay);
                printedCheckFileForVoids = Application.save(printedCheckFileForVoids);
                pPrintedCheckFiles.add(printedCheckFileForVoids);
            }
            for (VoidedCheck voidedCheck : voidedChecks) {
                try {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(voidedCheck.getCompany());
                    if (voidedCheck.getAgencyCheckBatch() != null) {
                        List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch(voidedCheck.getAgencyCheckBatch(), true);
                        SpcfDecimal amount = SpcfMoney.ZERO;
                        for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                            MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                            amount = amount.add(payment.getMoneyMovementTransactionAmount());

                        }

                        MoneyMovementTransaction representativePayment = voidedCheck.getAgencyCheckBatch().getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction();
                        PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo =
                                PaymentTemplatePrintedCheckInfo.findPaymentTemplatePrintedCheckInfo(representativePayment.getPaymentTemplate());
                        positivePayFile.addDetailRecord(representativePayment.findIntuitDebitAccount().getAccountNumber(),
                                representativePayment.getReferenceNumber(),
                                new SpcfMoney(amount),
                                representativePayment.getSettlementDate(),
                                paymentTemplatePrintedCheckInfo.getNameLine1(),
                                paymentTemplatePrintedCheckInfo.getNameLine2(),
                                true);

                    } else {
                        MoneyMovementTransaction payment = voidedCheck.getMoneyMovementTransaction();
                        PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo =
                                PaymentTemplatePrintedCheckInfo.findPaymentTemplatePrintedCheckInfo(payment.getPaymentTemplate());
                        positivePayFile.addDetailRecord(payment.findIntuitDebitAccount().getAccountNumber(),
                                payment.getReferenceNumber(),
                                payment.getMoneyMovementTransactionAmount(),
                                payment.getSettlementDate(),
                                paymentTemplatePrintedCheckInfo.getNameLine1(),
                                paymentTemplatePrintedCheckInfo.getNameLine2(),
                                true);
                    }


                    voidedCheck.setAccountingReportFile(printedCheckFileForVoids);
                    Application.save(voidedCheck);
                } finally {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
                }
            }

            if(positivePayFile.getCheckCount() >  0){
                writer = PgpWriterFactory.createInstance();
                writer.open(pFileName);
                positivePayFile.addHeader(writer, positiveFileAccountNumber);
                positivePayFile.writeDetailRecords(writer);
                positivePayFile.addTrailer(writer);
            }

        } finally {
            if(writer != null) {
                writer.close();
                sendEmail("psp_batch_bank_void_check_notify", pFileName);
            }
        }
    }

    private static String createReconPlusFile(DomainEntitySet<AccountingReportFile> pPrintedCheckFiles, String pFileName) throws Exception {
        PgpWriter fileWriter = new PgpCommonEncryptedWriter(BatchUtils.getTfaPgpKeys());
        fileWriter.open(pFileName);
        pFileName = ((PgpCommonEncryptedWriter) fileWriter).getEncryptedFileName();
        try {
            ReconPlusFile reconPlusFile = new ReconPlusFile();
            for (AccountingReportFile printedCheckFile : pPrintedCheckFiles) {
                DomainEntitySet<CheckPrintBatch> checkPrintBatches = printedCheckFile.getReconPlusFileBatchesCollection();
                checkPrintBatches = checkPrintBatches.sort(CheckPrintBatch.<CheckPrintBatch>CreatedDate());
                for (CheckPrintBatch checkPrintBatch : checkPrintBatches) {
                    List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch((AgencyCheckBatch)checkPrintBatch, false);
                    for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                        try {
                            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(paymentBatchAssociation.getCompany());
                            MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                            PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo =
                                    PaymentTemplatePrintedCheckInfo.findPaymentTemplatePrintedCheckInfo(payment.getPaymentTemplate());
                            int year = payment.getPaymentPeriodEnd().getYear();
                            int quarter = CalendarUtils.getQuarterAsInt(payment.getPaymentPeriodEnd());
                            reconPlusFile.writeDetailRecord(fileWriter, payment.getCreatorId(), payment.findIntuitDebitAccount().getAccountNumber(),
                                    payment.getReferenceNumber(),
                                    payment.getMoneyMovementTransactionAmount(),
                                    payment.getInitiationDate(),
                                    paymentTemplatePrintedCheckInfo.getNameLine1(),
                                    payment.getAgencyTaxpayerId(),
                                    payment.getCompany().getLegalName(),
                                    payment.getCompany().getSourceCompanyId(),
                                    payment.getCompany().getFedTaxId(),
                                    payment.getPaymentTemplate().getPaymentTemplateCd(),
                                    quarter, year);
                        } finally {
                            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
                        }

                    }
                }
            }
        } finally {
            fileWriter.close();
        }
        return pFileName;
    }

    private static void sendEmail(String pToFromProperty, String pFileName) {
        try {
            String toEmailAddress = BatchUtils.getConfigString(pToFromProperty);
            if(toEmailAddress != null && !toEmailAddress.contains("no_reply")) {

                boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
                if (enableEncryption && pFileName != null) {
                    PgpReader reader = PgpReaderFactory.createInstance();
                    reader.open(pFileName, PgpFileSourceCode.Intuit);

                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }

                    MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                        BatchUtils.getConfigString(pToFromProperty), // to
                        "psp_checks@intuit.com", // from
                        "Positive Pay File " + (Application.isProdEnvironment() ? "PROD" : "Non-PROD"),
                        "Positive Pay File for " + PSPDate.getPSPTime().toString(),
                        true,
                        null,
                        null,
                        stringBuilder.toString());
                } else {
                MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString(pToFromProperty), // to
                    "psp_checks@intuit.com", // from
                    "Positive Pay File " + (Application.isProdEnvironment() ? "PROD" : "Non-PROD"),
                    "Positive Pay File for " + PSPDate.getPSPTime().toString(),
                    pFileName);
                }
            } else {
                logger.warn("Email address " + pToFromProperty + " was null");
            }

        } catch (Exception e) {
            logger.error("Failed to send email message for positive pay file.", e);
        }
    }
}
