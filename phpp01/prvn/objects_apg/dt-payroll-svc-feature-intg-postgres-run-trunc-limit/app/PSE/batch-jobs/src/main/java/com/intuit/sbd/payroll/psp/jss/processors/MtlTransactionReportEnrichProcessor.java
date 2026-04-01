package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.batchjobs.mtl.MtlTransactionReportUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @author kmuthurangam
 * <p>
 * State regulators require various reports to be submitted throughout the year to ensure continual compliance.  Regulators monitor transactional volume, financial condition,and information
 * submitted by customers.
 * </p>
 * <p>
 * Money Trasmitter License (MTL) Transactional report is one such report that needs to be submitted to Regulators to ensure continual compliance. Risk team will provide the raw MTL
 * Transaction report without few of the sensitive PII information and this job intends to enrich the raw Transactional data with additional PII information as well as missing information.
 * </p>
 * <p>
 * This jobs downloads the raw MTL Transaction report from risk team S3 bucket, enriches and upload the same into their S3 bucket, in addition it also archives the files in PSP S3 bucket for
 * furture investigation.
 * </p>
 */
// TODO Add Integration tests
@ScheduledJob(name = "MtlTransactionReportEnrichProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class MtlTransactionReportEnrichProcessor extends JSSBatchJob {

    public MtlTransactionReportEnrichProcessor(String[] pArguments) {
        super(pArguments);
    }

    public MtlTransactionReportEnrichProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() throws Exception {
        getLogger().info("Starting MtlTransactionReportEnrichProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(DownloadMtlTransactionReport.class);
        executeStep(EnrichMtlTransactionReport.class);
        executeStep(UploadEnrichedMtlTransactionReport.class);
        executeStep(ArchiveEnrichedMtlTransactionReport.class);

        getLogger().info("Completed MtlTransactionReportEnrichProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class DownloadMtlTransactionReport extends JSSBatchJobStep<MtlTransactionReportEnrichProcessor> {
        @Override
        protected void execute() throws Exception {
            MtlTransactionReportUtils.downloadAllReports();
        }
    }

    public static class EnrichMtlTransactionReport extends JSSBatchJobStep<MtlTransactionReportEnrichProcessor> {
        @Override
        protected void execute() throws Exception {
            MtlTransactionReportUtils.enrichAllReports();
        }
    }

    public static class UploadEnrichedMtlTransactionReport extends JSSBatchJobStep<MtlTransactionReportEnrichProcessor> {
        @Override
        protected void execute() throws Exception {
            // TODO Enable encryption & decryption
            //MtlTransactionReportUtils.uploadAllEnrichedReports();
        }
    }

    public static class ArchiveEnrichedMtlTransactionReport extends JSSBatchJobStep<MtlTransactionReportEnrichProcessor> {
        @Override
        protected void execute() throws Exception {
            MtlTransactionReportUtils.encryptAndArchiveAllReports();
            FileUtils.cleanDirectory(new File(MtlTransactionReportUtils.getMtlWorkFolder()));
        }
    }
}
