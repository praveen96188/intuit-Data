package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.ems.payroll.psp.gateway.brm.BRMAssistedUsageFileUploader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.AssistedUsageReport.AssistedUsageToBRMDataSyncFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.billing.EMSBSToBRMSyncFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.util.BRMUtils;
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

@ScheduledJob(name = "AssistedUsageReportingToBRMProcessor",resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class AssistedUsageReportingToBRMProcessor extends JSSBatchJob {
    public AssistedUsageReportingToBRMProcessor(String[] pArguments) {
        super(pArguments);
    }

    public AssistedUsageReportingToBRMProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        executeStep(GenerateAssistedUsageToBRMDataSyncFile.class);
        executeStep(UploadAssistedUsageBRMDataSyncFile.class);
    }

    public static class GenerateAssistedUsageToBRMDataSyncFile extends JSSBatchJobStep<AssistedUsageReportingToBRMProcessor> {
        @Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AssistedUsageReportProcess));
                new AssistedUsageToBRMDataSyncFileGenerator().generate();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step GenerateAssistedUsageToBRMDataSyncFile ", t);
            }
        }
    }

    public static class UploadAssistedUsageBRMDataSyncFile extends JSSBatchJobStep<AssistedUsageReportingToBRMProcessor> {
        @Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AssistedUsageReportProcess));
                Boolean isEncryptionEnabled= FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BRM_IDPS_ENCRPYTION_ENABLED, false);
                if(isEncryptionEnabled){
                    new BRMUtils().performIdpsEncryption(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_work_dir"));
                }
                new BRMAssistedUsageFileUploader().upload();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UploadDiamondBillsToBRMDataSyncFile ", t);
            }
        }
    }
}
