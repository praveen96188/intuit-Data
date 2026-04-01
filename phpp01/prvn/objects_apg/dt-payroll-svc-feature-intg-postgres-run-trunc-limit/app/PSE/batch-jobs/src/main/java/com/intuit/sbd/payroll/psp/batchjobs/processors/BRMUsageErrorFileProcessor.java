package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.ems.payroll.psp.gateway.brm.BRMFileUploader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.billing.ProcessErrorCsv;
import com.intuit.sbd.payroll.psp.batchjobs.util.BRMUtils;
import com.intuit.sbd.payroll.psp.common.utils.BRMS3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * User: sshetty
 * Date: 12/9/13
 */
public class BRMUsageErrorFileProcessor extends BatchJobProcessor {
    static final SpcfLogger logger = SpcfLogManager.getLogger(BRMUsageErrorFileProcessor.class);
    private String mFileName = null;

    public BRMUsageErrorFileProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {

        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void validateRuntimeParameters() {

        super.validateRuntimeParameters();
        String args = getJobInstanceParameters();
        if(args != null) {
        mFileName = args.trim();
        }

    }


    @Override
    protected void execute() {
        StopWatch timer = StopWatch.startTimer();
        logger.info("Starting " + getClass().getSimpleName() + " process job");
        executeStep(new DownloadUsageErrorFile());
        executeStep(new ProcessUsageErrorFile());
        executeStep(new UploadUsageErrorFile());
        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public class DownloadUsageErrorFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                StopWatch timer = StopWatch.startTimer();
                logger.info("Starting DownloadUsageErrorFile step");
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BRMUsageErrorFileProcess));
                boolean isEncryptionEnabled= FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BRM_IDPS_ENCRPYTION_ENABLED, false);
                if(isEncryptionEnabled){
                    BRMS3UploadUtils.downloadEncryptedFileFromS3();
                }else{
                    BRMS3UploadUtils.downloadFileFromS3();
                }

                logger.info("Completed DownloadUsageErrorFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step DownloadUsageErrorFile ", t);
            }
        }
    }

    public class ProcessUsageErrorFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                StopWatch timer = StopWatch.startTimer();
                logger.info("Starting ProcessUsageErrorFile step");
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BRMUsageErrorFileProcess));
                new ProcessErrorCsv().processFiles();
                logger.info("Completed ProcessUsageErrorFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessUsageErrorFile ", t);
            }

        }
    }

    public class UploadUsageErrorFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                StopWatch timer = StopWatch.startTimer();
                logger.info("Starting UploadUsageErrorFile step");
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BRMUsageErrorFileProcess));
                boolean isEncryptionEnabled= FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BRM_IDPS_ENCRPYTION_ENABLED, false);
                if(isEncryptionEnabled){
                    new BRMUtils().performIdpsEncryption(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_work_dir"));
                }
                new BRMFileUploader().uploadBRMUsageFiles();

                logger.info("Completed UploadUsageErrorFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessUsageErrorFile ", t);
            }
        }
    }

}


