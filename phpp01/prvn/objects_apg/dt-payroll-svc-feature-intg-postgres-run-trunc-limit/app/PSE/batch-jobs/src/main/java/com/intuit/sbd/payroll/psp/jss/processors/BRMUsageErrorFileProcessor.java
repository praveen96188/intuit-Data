package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.ems.payroll.psp.gateway.brm.BRMFileUploader;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.billing.ProcessErrorCsv;
import com.intuit.sbd.payroll.psp.batchjobs.util.BRMUtils;
import com.intuit.sbd.payroll.psp.common.utils.BRMS3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: sshetty
 * Date: 12/9/13
 */
@ScheduledJob(name = "BRMUsageErrorFileProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class BRMUsageErrorFileProcessor extends JSSBatchJob {

    private String mFileName = null;

    public BRMUsageErrorFileProcessor(String[] pArguments) {
        super(pArguments);
    }

    public BRMUsageErrorFileProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
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
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        executeStep(DownloadUsageErrorFile.class);
        executeStep(ProcessUsageErrorFile.class);
        executeStep(UploadUsageErrorFile.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class DownloadUsageErrorFile extends JSSBatchJobStep<BRMUsageErrorFileProcessor> {
        public void execute() {
            try {
                StopWatch timer = StopWatch.startTimer();
                getLogger().info("Starting DownloadUsageErrorFile step");
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BRMUsageErrorFileProcess));
                boolean isEncryptionEnabled= FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BRM_IDPS_ENCRPYTION_ENABLED, false);
                if(isEncryptionEnabled){
                    BRMS3UploadUtils.downloadEncryptedFileFromS3();
                }else{
                    BRMS3UploadUtils.downloadFileFromS3();
                }
                getLogger().info("Completed DownloadUsageErrorFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step DownloadUsageErrorFile ", t);
            }
        }
    }

    public static class ProcessUsageErrorFile extends JSSBatchJobStep<BRMUsageErrorFileProcessor> {
        public void execute() {
            try {
                StopWatch timer = StopWatch.startTimer();
                getLogger().info("Starting ProcessUsageErrorFile step");
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BRMUsageErrorFileProcess));
                new ProcessErrorCsv().processFiles();
                getLogger().info("Completed ProcessUsageErrorFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessUsageErrorFile ", t);
            }

        }
    }

    public static class UploadUsageErrorFile extends JSSBatchJobStep<BRMUsageErrorFileProcessor> {
        public void execute() {
            try {
                StopWatch timer = StopWatch.startTimer();
                getLogger().info("Starting UploadUsageErrorFile step");
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BRMUsageErrorFileProcess));
                boolean isEncryptionEnabled= FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BRM_IDPS_ENCRPYTION_ENABLED, false);
                if(isEncryptionEnabled){
                    new BRMUtils().performIdpsEncryption(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_work_dir"));
                }
                new BRMFileUploader().uploadBRMUsageFiles();

                getLogger().info("Completed UploadUsageErrorFile step. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessUsageErrorFile ", t);
            }
        }
    }

}


