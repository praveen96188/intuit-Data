package com.intuit.sbd.payroll.psp.jss.processors.reencryption;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3DownloadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

@ScheduledJob(name = "DataReencryptionProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class DataReencryptionProcessor extends JSSBatchJob {
    private static final SpcfLogger LOGGER = SpcfLogManager.getLogger(DataReencryptionProcessor.class);

    private static final String PARAM_FILE_S3_LOCATION_FOLDER = "apps/batch/flux/work/reencryption/";

    private static final String PARAM_FILE_LOCAL_LOCATION_FOLDER = "/tmp/";

    private static String param_file_name;

    public DataReencryptionProcessor(String[] pArguments) {
        super(pArguments);
    }

    public DataReencryptionProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        String commandLine = getJobInstanceParameters().trim();
        if (commandLine.length() > 1) {
            String[] args = commandLine.split(" ");
            if (args.length == 0 || args.length > 1) {
                throw new RuntimeException("Expected argument: File name present in " + PARAM_FILE_S3_LOCATION_FOLDER);
            }
            param_file_name = args[0];
            System.setProperty("paramFile", PARAM_FILE_LOCAL_LOCATION_FOLDER + param_file_name);
            LOGGER.info("Param File property=" + System.getProperty("paramFile") + ", Project Name property=" + System.getProperty("project.name"));
        } else {
            throw new RuntimeException("Please specify file name as command line argument");
        }


    }

    @Override
    protected void execute() throws Exception {
        LOGGER.info("job=data_reencryption_processor,action=execute_starting");

        StopWatch timer = StopWatch.startTimer();
        executeStep(StartEncryptionProcessor.class);

        LOGGER.info("job=data_reencryption_processor,action=execute_completed.elapsed_time=" + timer.stop().getElapsedMillis());
    }

    public static class StartEncryptionProcessor extends JSSBatchJobStep<DataReencryptionProcessor> {

        @Override
        protected void execute() throws Exception {
            try {
                LOGGER.info("job=data_reencryption_processor,action=execute_starting,step");
                downloadParamFileFromS3();

                try {
                    PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.DataReencryptionProcessor));

                    PayrollServices.beginUnitOfWork();

                    EncryptionProcess encryptionProcess = new EncryptionProcessFactory().getEncryptionProcess(DataReEncryptionUtils.getDatabaseTypeFromParamFile(PARAM_FILE_LOCAL_LOCATION_FOLDER + param_file_name));

                    encryptionProcess.startEncryption(PARAM_FILE_LOCAL_LOCATION_FOLDER + param_file_name);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step EncryptionProcess ", t);
            }
        }

        private void downloadParamFileFromS3() throws S3DownloadException, S3ConnectionException {
            File file = new File("/tmp", FilenameUtils.getName(param_file_name));
            file.delete();
            String bucketName = BatchUtils.getConfigString(S3UploadUtils.PSP_BATCHJOBS_S3_BUCKET);
            LOGGER.info("Downloading file from S3 bucket=" + bucketName + " location=" + PARAM_FILE_S3_LOCATION_FOLDER + param_file_name);
            S3UploadUtils.downloadFromS3FileStore(bucketName, PARAM_FILE_S3_LOCATION_FOLDER + param_file_name, file.toString());
            LOGGER.info("File downloaded successfully at=" + PARAM_FILE_LOCAL_LOCATION_FOLDER + param_file_name);
        }

    }
}