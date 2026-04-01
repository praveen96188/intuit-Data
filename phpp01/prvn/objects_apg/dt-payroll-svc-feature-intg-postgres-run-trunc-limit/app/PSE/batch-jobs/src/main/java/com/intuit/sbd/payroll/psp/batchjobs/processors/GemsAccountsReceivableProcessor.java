package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpGemsFileUpload;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload.DailyGemsUploadBatchProcess;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.ReportingFrequency;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatch;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatchStatus;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 13, 2009
 * Time: 6:07:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GemsAccountsReceivableProcessor extends BatchJobProcessor {
    private String mBatchId = null;
    private SpcfCalendar mOffloadDate = null;

    public GemsAccountsReceivableProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    public String getBatchId() {
        return mBatchId;
    }

    public SpcfCalendar getOffloadDate() {
        return mOffloadDate;
    }

    protected void validateRuntimeParameters() {
        mOffloadDate =  PSPDate.getPSPTime();
        mBatchId="0";
        String commandLine = getJobInstanceParameters().trim();
        String[] args = commandLine.split(" ");
        if (args.length > 0) {
            for (String arg : args) {
                // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                    SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                    mOffloadDate = SpcfCalendar.createInstance(clDate.getYear(),
                                                                 clDate.getMonth(),
                                                                 clDate.getDay(),
                                                                 0, 0,0,0,SpcfTimeZone.getLocalTimeZone());
                } else {
                    mBatchId=arg;
                }
            }
        }
    }

    protected void validateStepRuntimeParameters(String stepName) {
        if (CreateGemsAccountsReceivableFile.class.getSimpleName().equals(stepName)) {
            validateRuntimeParameters();
        } else if (UploadGemsAccountsReceivableFile.class.getSimpleName().equals(stepName)) {
            // no validation
        } else if (ArchiveGemsAccountsReceivableFile.class.getSimpleName().equals(stepName)) {
            // no validation
        } else {
            StringBuffer err = new StringBuffer();

            err.append("The specified job step \"").
                append(stepName).
                append("\" does not exist in batch processor ").
                append(this.getClass().getSimpleName()).
                append(". The valid steps (with optional arguments) that can be executed are {").
                append(CreateGemsAccountsReceivableFile.class.getSimpleName()).append(" [batch-id], ").
                append(UploadGemsAccountsReceivableFile.class.getSimpleName()).append(", ").
                append(ArchiveGemsAccountsReceivableFile.class.getSimpleName()).append("}");

            throw new RuntimeException(err.toString());
        }
    }

    protected void execute() {
        logger.info("Starting GEMS A/R batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new CreateGemsAccountsReceivableFile());
        executeStep(new UploadGemsAccountsReceivableFile());
        executeStep(new ArchiveGemsAccountsReceivableFile());

        logger.info("Completed GEMS A/R batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Job Steps
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class CreateGemsAccountsReceivableFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsAccountsReceivableBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new DailyGemsUploadBatchProcess().createFile(getBatchId(),getOffloadDate());

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateGemsAccountsReceivableFile ", t);
            }
        }
    }

    public class UploadGemsAccountsReceivableFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsAccountsReceivableBatchJob);

                new SftpGemsFileUpload().upload(ReportingFrequency.Daily);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UploadGemsAccountsReceivableFile ", t);
            }
        }
    }

    public class ArchiveGemsAccountsReceivableFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsAccountsReceivableBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

                    DomainEntitySet<GemsUploadBatch> batchSet =
                            BatchUtils.getGemsUploadFilesByStatus(ReportingFrequency.Daily,
                                                                  GemsUploadBatchStatus.Transmitted);

                    for (GemsUploadBatch batch : batchSet) {
                        BatchUtils.moveFile(batch.getFileName(), archiveDir);

                        batch.setUploadStatus(GemsUploadBatchStatus.Archived);
                        batch.setStatusEffectiveDate(PSPDate.getPSPTime());

                        Application.save(batch);
                    }

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveGemsAccountsReceivableFile ", t);
            }
        }
    }
}
