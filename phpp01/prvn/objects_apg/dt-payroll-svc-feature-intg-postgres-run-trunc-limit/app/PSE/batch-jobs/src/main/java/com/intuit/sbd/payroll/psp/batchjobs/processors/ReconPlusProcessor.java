package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.ReconPlus.AccountsFlatFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedCheckFlatFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpCheckFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 15, 2011
 * Time: 4:39:50 PM
 */
public class ReconPlusProcessor extends BatchJobProcessor {

    private SpcfCalendar mRunDate;
    private Boolean mPriorDate = false;

    public SpcfCalendar getRunDate() {
        return mRunDate;
    }

    public void setRunDate(SpcfCalendar pRunDate) {
        mRunDate = pRunDate;
    }

    public Boolean isPriorDate() {
        return mPriorDate;
    }

    public void setIsPriorDate(Boolean pPriorDate) {
        mPriorDate = pPriorDate;
    }

    public ReconPlusProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        String commandLine = getJobInstanceParameters().trim();

        if ((getRunMode() == RunMode.UsingFlux) || (commandLine.length() == 0)) {
            setRunDate(now.copy());
        }
        else {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                        setRunDate(SpcfCalendar.createInstance(clDate.getYear(),
                                clDate.getMonth(),
                                clDate.getDay(),
                                0, 0, 0, 0,
                                SpcfTimeZone.getLocalTimeZone()));
                        setIsPriorDate(true);
                    }
                }
            }
        }
    }

    @Override
    protected void validateStepRuntimeParameters(String stepName) {
        validateRuntimeParameters();
    }

    @Override
    protected void execute() {
        logger.info("Starting recon plus processor");
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ReconPlusBatchJob));

        /*  Create the files    */
        executeStep(new CreateACHReconPlusFile());
        //executeStep(new CreateReturnsReconPlusFile());
        executeStep(new CreateCheckReconPlusFile());
        /*  Attempt upload */
        executeStep(new UploadTaxAccountsReconPlusFiles());
        executeStep(new UploadCheckReconPlusFiles());
//        executeStep(new UploadReturnsAccountsReconPlusFiles());
        /*  Archive all of them */
        executeStep(new ArchiveReconPlusFiles());

        logger.info("Completed recon plus processor. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class CreateCheckReconPlusFile extends BatchJobProcessorStep {
         public void execute() {
             try {
                 if (isPriorDate()) {
                     PayrollServices.beginUnitOfWork();
                     DomainEntitySet<CheckPrintBatch> batches = CheckPrintBatch.getBatchForDate(getRunDate());

                     for (CheckPrintBatch batch : batches) {
                         AccountingReportFile createdFile = batch.getReconPlusFile();
                         if (createdFile != null) {
                             createdFile.setStatus(AccountingReportFileStatus.New);
                             createdFile.setTransmissionDate(null);
                             createdFile.setFileName(null);
                             Application.save(createdFile);
                         }
                     }
                     PayrollServices.commitUnitOfWork();
                 }
                 PayrollServices.beginUnitOfWork();
                 PrintedCheckFlatFileGenerator.createFile(AccountingReportFileType.PrintedCheckReconPlus);
                 PayrollServices.commitUnitOfWork();
             } catch (Throwable t) {
                 throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
             } finally {
                 PayrollServices.rollbackUnitOfWork();
             }
         }
    }

    public class CreateACHReconPlusFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                AccountsFlatFileGenerator.createACHAccountsFile(AccountingReportFileType.TaxAccountsReconPlus, getRunDate());
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class CreateReturnsReconPlusFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                AccountsFlatFileGenerator.createACHAccountsFile(AccountingReportFileType.ReturnsAccountsReconPlus, getRunDate());
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class UploadCheckReconPlusFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                new SftpCheckFileUpload().upload(AccountingReportFileType.PrintedCheckReconPlus);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            }
        }
    }

    public class UploadTaxAccountsReconPlusFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                new SftpCheckFileUpload().upload(AccountingReportFileType.TaxAccountsReconPlus);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            }
        }
    }

    public class UploadReturnsAccountsReconPlusFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                new SftpCheckFileUpload().upload(AccountingReportFileType.ReturnsAccountsReconPlus);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            }
        }
    }

    public class ArchiveReconPlusFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();

                String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

                DomainEntitySet<AccountingReportFile> printedCheckFiles =
                        Application.find(AccountingReportFile.class, AccountingReportFile.Status().equalTo(AccountingReportFileStatus.Transmitted)
                                .And(AccountingReportFile.Type().in(AccountingReportFileType.PrintedCheckReconPlus, AccountingReportFileType.TaxAccountsReconPlus, AccountingReportFileType.ReturnsAccountsReconPlus)));

                for (AccountingReportFile printedCheckFile : printedCheckFiles) {
                    // archive the file
                    File destinationFile = BatchUtils.moveFile(printedCheckFile.getFileName(), archiveDir);
                    printedCheckFile.setStatus(AccountingReportFileStatus.Archived);
                    printedCheckFile.setFileName(destinationFile.getAbsolutePath());
                    Application.save(printedCheckFile);
                }
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }
}
