package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedCheckFlatFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedChecksSelector;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpCheckFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 30, 2011
 * Time: 1:45:38 PM
 */
@ScheduledJob(name = "PrintedCheckBatch",  resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class PrintedCheckBatchProcessor extends JSSBatchJob {

	public PrintedCheckBatchProcessor(String[] pArguments) {
		super(pArguments);
	}

	public PrintedCheckBatchProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

    @Override
    protected void execute() {
    	getLogger().info("Starting processing printed check batches");
        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.PrintedCheckBatchJob));
        
        executeStep(CreateCheckBatches.class);       
        executeStep(CreateSuperCheckBatches.class);
        executeStep(CreatePositivePayFile.class);        
        executeStep(UploadPositivePayFiles.class);
        executeStep(ArchiveFiles.class);

        getLogger().info("Completed processing printed check batches. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class CreateCheckBatches extends JSSBatchJobStep<PrintedCheckBatchProcessor> {
        public void execute() {
            try {
                new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateCheckBatches ", t);
            }
        }
    }

    public static class CreateSuperCheckBatches extends JSSBatchJobStep<PrintedCheckBatchProcessor> {
        public void execute() {
            try {
                new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.SuperCheck);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateCheckBatches ", t);
            }
        }
    }

    public static class CreatePositivePayFile extends JSSBatchJobStep<PrintedCheckBatchProcessor> {
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

    public static class UploadPositivePayFiles extends JSSBatchJobStep<PrintedCheckBatchProcessor> {
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

    public static class ArchiveFiles extends JSSBatchJobStep<PrintedCheckBatchProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();

                String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

                DomainEntitySet<AccountingReportFile> printedCheckFiles =
                        Application.find(AccountingReportFile.class, AccountingReportFile.Status().equalTo(AccountingReportFileStatus.Transmitted)
                                .And(AccountingReportFile.Type().equalTo(AccountingReportFileType.PositivePay)));

                for (AccountingReportFile printedCheckFile : printedCheckFiles) {
                    // archive the file
                    String batchJobName = BatchJobType.PrintedCheckBatch.name();
                    printedCheckFile.setFileName(S3UploadUtils.archive(batchJobName,archiveDir,printedCheckFile.getFileName()));
                    printedCheckFile.setStatus(AccountingReportFileStatus.Archived);
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
