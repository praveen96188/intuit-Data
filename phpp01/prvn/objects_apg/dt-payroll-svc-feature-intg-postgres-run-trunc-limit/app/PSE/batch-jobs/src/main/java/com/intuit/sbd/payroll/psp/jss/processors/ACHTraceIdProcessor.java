package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ACHTraceIdFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpAchTraceFileDownload;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/***
 * ACHTraceIdProcessor job
 * Processes the ACH TraceId file provided by JPMC for each ACH file uploaded.
 * The job update the JPMC tracenumber for each of the money movement transactions offloaded and the JPMC trace number gets stored in the EDR table
 *
 * Note : Currently, we are not storing the metadata of the file downloaded in the database, In case of failures, the filename needs to be fetched from splunk logs to reprocess
 */
@ScheduledJob(name = "ACHTraceIdProcessor", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ACHTraceIdProcessor extends JSSBatchJob {

    List<String> achTraceIdFileNames = null;

    public ACHTraceIdProcessor(String[] pArguments) {
        super(pArguments);
    }

    public ACHTraceIdProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }


    protected void validateRuntimeParameters() {
        achTraceIdFileNames = new ArrayList<String>();
        // getJobInstanceParameters() will never return null
        String commandLine = getJobInstanceParameters().trim();
        if(StringUtils.isNotBlank(commandLine)){
            String[] args = commandLine.split(" ");
            for (String arg : args){
                achTraceIdFileNames.add(arg);
            }
        }
    }

    @Override
    protected void execute() throws Exception {

        StopWatch timer = StopWatch.startTimer();
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        executeStep(DownloadACHTraceIdFile.class);
        executeStep(ParseAndPersistData.class);
        executeStep(ArchiveACHTraceIdFile.class);
        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class DownloadACHTraceIdFile extends JSSBatchJobStep<ACHTraceIdProcessor> {

        @Override
        protected void execute() throws Exception {
            StopWatch timer = StopWatch.startTimer();
            getLogger().info("Starting DownloadACHTraceIdFile step");
            List<String> fileList = new SftpAchTraceFileDownload().download();
            getBatchJobProcessor().achTraceIdFileNames.addAll(fileList);
            getLogger().info("Completed DownloadACHTraceIdFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }

    public static class ParseAndPersistData extends JSSBatchJobStep<ACHTraceIdProcessor> {

        @Override
        protected void execute() throws Exception {
            StopWatch timer = StopWatch.startTimer();
            getLogger().info("Starting ParseAndPersistData step");
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ACHTraceIdProcessor));
            String ftp_recv_dir = BatchUtils.getConfigString("psp_batch_ftp_recv_dir");

            ACHTraceIdFileParser achTraceIdFileParser = new ACHTraceIdFileParser();
            for (String fileName : getBatchJobProcessor().achTraceIdFileNames) {
                achTraceIdFileParser.parseAndUpdateTraceId(ftp_recv_dir + "/" + fileName);
            }
            getLogger().info("Completed ParseAndPersistData step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }

    public static class ArchiveACHTraceIdFile extends JSSBatchJobStep<ACHTraceIdProcessor> {

        @Override
        protected void execute() throws Exception {
            StopWatch timer = StopWatch.startTimer();
            getLogger().info("Starting ArchiveACHTraceIdFile step");
            String ftpRecvDir = BatchUtils.getConfigString("psp_batch_ftp_recv_dir");
            String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
            String batchJobName = BatchJobType.ACHTraceIdProcessor.name();
            for (String fileName : getBatchJobProcessor().achTraceIdFileNames) {
                S3UploadUtils.archive(batchJobName, archiveDir, ftpRecvDir + "/" + fileName);
            }
            getLogger().info("Completed ArchiveACHTraceIdFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }
}
