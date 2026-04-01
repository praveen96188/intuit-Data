package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

import com.intuit.sbd.payroll.psp.batchjobs.ReconPlus.AccountsFlatFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedCheckFlatFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpCheckFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 15, 2011
 * Time: 4:39:50 PM
 */
@ScheduledJob(name = "ReconPlus", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ReconPlusProcessor extends JSSBatchJob {

    private SpcfCalendar mRunDate;
    private Boolean mPriorDate = false;
    
    public ReconPlusProcessor(String[] pArguments) {
		super(pArguments);
	}

	public ReconPlusProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

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

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        String commandLine = getJobInstanceParameters().trim();

        if (commandLine.length() == 0) {
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
    	getLogger().info("Starting recon plus processor");
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
        	getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ReconPlusBatchJob));

        /*  Create the files    */
        executeStep( CreateACHReconPlusFile.class);
        //executeStep(new CreateReturnsReconPlusFile());
        executeStep(CreateCheckReconPlusFile.class);
        /*  Attempt upload */
        executeStep(UploadTaxAccountsReconPlusFiles.class);
        executeStep(UploadCheckReconPlusFiles.class);
//        executeStep(new UploadReturnsAccountsReconPlusFiles());
        /*  Archive all of them */
        executeStep(ArchiveReconPlusFiles.class);

        getLogger().info("Completed recon plus processor. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class CreateCheckReconPlusFile extends  JSSBatchJobStep<ReconPlusProcessor> {
         public void execute() {
             try {
                 if (getBatchJobProcessor().isPriorDate()) {
                     PayrollServices.beginUnitOfWork();
                     DomainEntitySet<CheckPrintBatch> batches = CheckPrintBatch.getBatchForDate(getBatchJobProcessor().getRunDate());

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

    public static class CreateACHReconPlusFile extends JSSBatchJobStep<ReconPlusProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                AccountsFlatFileGenerator.createACHAccountsFile(AccountingReportFileType.TaxAccountsReconPlus, getBatchJobProcessor().getRunDate());
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class CreateReturnsReconPlusFile extends JSSBatchJobStep<ReconPlusProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                AccountsFlatFileGenerator.createACHAccountsFile(AccountingReportFileType.ReturnsAccountsReconPlus, getBatchJobProcessor().getRunDate());
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class UploadCheckReconPlusFiles extends  JSSBatchJobStep<ReconPlusProcessor> {
        public void execute() {
            try {
                new SftpCheckFileUpload().upload(AccountingReportFileType.PrintedCheckReconPlus);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            }
        }
    }

    public static class UploadTaxAccountsReconPlusFiles extends JSSBatchJobStep<ReconPlusProcessor> {
        public void execute() {
            try {
                new SftpCheckFileUpload().upload(AccountingReportFileType.TaxAccountsReconPlus);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            }
        }
    }

    public static  class UploadReturnsAccountsReconPlusFiles extends JSSBatchJobStep<ReconPlusProcessor> {
        public void execute() {
            try {
                new SftpCheckFileUpload().upload(AccountingReportFileType.ReturnsAccountsReconPlus);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step " + getClass().getSimpleName(), t);
            }
        }
    }

    public static  class ArchiveReconPlusFiles extends JSSBatchJobStep<ReconPlusProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();

                String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
                String batchJobName = BatchJobType.ReconPlus.name();

                DomainEntitySet<AccountingReportFile> printedCheckFiles =
                        Application.find(AccountingReportFile.class, AccountingReportFile.Status().equalTo(AccountingReportFileStatus.Transmitted)
                                .And(AccountingReportFile.Type().in(AccountingReportFileType.PrintedCheckReconPlus, AccountingReportFileType.TaxAccountsReconPlus, AccountingReportFileType.ReturnsAccountsReconPlus)));

                for (AccountingReportFile printedCheckFile : printedCheckFiles) {
                    // archive the file
                    printedCheckFile.setFileName(S3UploadUtils.archive(batchJobName,archiveDir,printedCheckFile.getFileName()));
                    printedCheckFile.setStatus(AccountingReportFileStatus.Archived);
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
