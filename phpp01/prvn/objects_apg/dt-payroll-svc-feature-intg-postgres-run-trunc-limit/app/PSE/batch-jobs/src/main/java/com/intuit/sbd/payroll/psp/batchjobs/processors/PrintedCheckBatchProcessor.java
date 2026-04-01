package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedCheckFlatFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedChecksSelector;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpCheckFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 30, 2011
 * Time: 1:45:38 PM
 */
public class PrintedCheckBatchProcessor extends BatchJobProcessor {

    public PrintedCheckBatchProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting processing printed check batches");
        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.PrintedCheckBatchJob));
        
        executeStep(new CreateCheckBatches());
        executeStep(new CreateSuperCheckBatches());
        executeStep(new CreatePositivePayFile());        
        executeStep(new UploadPositivePayFiles());
        executeStep(new ArchiveFiles());

        logger.info("Completed processing printed check batches. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class CreateCheckBatches extends BatchJobProcessorStep {
        public void execute() {
            try {
                new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateCheckBatches ", t);
            }
        }
    }

    public class CreateSuperCheckBatches extends BatchJobProcessorStep {
        public void execute() {
            try {
                new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.SuperCheck);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateCheckBatches ", t);
            }
        }
    }

    public class CreatePositivePayFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                PrintedCheckFlatFileGenerator.createFile(AccountingReportFileType.PositivePay);
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreatePositivePayFile ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class UploadPositivePayFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                boolean uploadFilesToBank = SystemParameter.findBooleanValue(SystemParameter.Code.PRINTED_CHECKS_UPLOAD_POSITIVE_PAY_FILES, true);
                PayrollServices.rollbackUnitOfWork();

                if(uploadFilesToBank) {
                    new SftpCheckFileUpload().upload(AccountingReportFileType.PositivePay);
                } else {
                    PayrollServices.beginUnitOfWork();
                    DomainEntitySet<AccountingReportFile> accountingReportFiles =
                            AccountingReportFile.findByTypeAndStatus(AccountingReportFileType.PositivePay, AccountingReportFileStatus.Created, false);
                    for (AccountingReportFile accountingReportFile : accountingReportFiles) {
                        accountingReportFile.setStatus(AccountingReportFileStatus.Transmitted);
                        Application.save(accountingReportFile);
                    }
                    PayrollServices.commitUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UploadPositivePayFiles ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class ArchiveFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();

                String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

                DomainEntitySet<AccountingReportFile> printedCheckFiles =
                        Application.find(AccountingReportFile.class, AccountingReportFile.Status().equalTo(AccountingReportFileStatus.Transmitted)
                                .And(AccountingReportFile.Type().equalTo(AccountingReportFileType.PositivePay)));

                for (AccountingReportFile printedCheckFile : printedCheckFiles) {
                    // archive the file
                    File destFile = BatchUtils.moveFile(printedCheckFile.getFileName(), archiveDir);
                    printedCheckFile.setStatus(AccountingReportFileStatus.Archived);
                    printedCheckFile.setFileName(destFile.getAbsolutePath());
                    Application.save(printedCheckFile);

                    // update all of the payments
                    for (CheckPrintBatch checkPrintBatch : printedCheckFile.getPositivePayFileBatchesCollection()) {
                        List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch((AgencyCheckBatch)checkPrintBatch, true);
                        for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                            MoneyMovementTransaction moneyMovementTransaction = paymentBatchAssociation.getMoneyMovementTransaction();
                            moneyMovementTransaction.updateTaxPaymentStatus(TaxPaymentStatus.SentToAgency, false, true);
                            moneyMovementTransaction.updateTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency, true, true);
                        }
                    }
                }

                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveFiles ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }
}
