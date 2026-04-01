package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.ems.payroll.psp.gateway.brm.BRMFileUploader;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.billing.EMSBSToBRMSyncFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.util.BRMUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/23/12
 * Time: 3:18 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "EMSBSToBRMDataSyncProcessor",resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EMSBSToBRMDataSyncProcessor extends JSSBatchJob {
	static final SpcfLogger logger = SpcfLogManager.getLogger(EMSBSToBRMDataSyncProcessor.class);
	private static SpcfCalendar mProcessingDate;

	public EMSBSToBRMDataSyncProcessor(String[] pArguments) {
        super(pArguments);
	}
	public EMSBSToBRMDataSyncProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}

	@Override
	protected void execute() {
		getLogger().info("Starting " + getClass().getSimpleName() + " process job. Processing Date=" + mProcessingDate);
		executeStep(GenerateEMSBSToBRMDataSyncFile.class);
        executeStep(UploadEMSBSToBRMDataSyncFile.class);
	}

	public static class GenerateEMSBSToBRMDataSyncFile extends JSSBatchJobStep<EMSBSToBRMDataSyncProcessor> {
		@Override
		public void execute() {
			try {
				PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EMSBSToBRMDataSyncBatchJob));

				new EMSBSToBRMSyncFileGenerator().generate(mProcessingDate);
			} catch (Throwable t) {
				throw new RuntimeException("Exception in job step GenerateEMSBSToBRMDataSyncFile ", t);
			}
		}
	}

    public static class UploadEMSBSToBRMDataSyncFile extends JSSBatchJobStep<EMSBSToBRMDataSyncProcessor> {
        @Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EMSBSToBRMDataSyncBatchJob));
				Boolean isEncryptionEnabled= FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BRM_IDPS_ENCRPYTION_ENABLED, false);
				logger.info("UploadEMSBSToBRMDataSyncFile: IsEncryption Enabled- "+isEncryptionEnabled);
				if(isEncryptionEnabled){
					new BRMUtils().performIdpsEncryption(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_work_dir"));
				}
                new BRMFileUploader().upload();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UploadEMSBSToBRMDataSyncFile ", t);
            }
        }
    }

	@Override
	protected void validateRuntimeParameters() {
		String pCommandLineArg = getJobInstanceParameters().trim();
		logger.info("Command Line Arguments: " + pCommandLineArg);

		try {
			Application.beginUnitOfWork();
			mProcessingDate = PSPDate.getPSPTime();
			CalendarUtils.clearTime(mProcessingDate);
		} finally {
			Application.rollbackUnitOfWork();
		}

		String[] args = null;
		if (pCommandLineArg.trim().length() > 0) {
			args = pCommandLineArg.split(" ");
		}
		if (args == null || args.length == 0)
			return;

		if (!args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
			throw new RuntimeException("Invalid processing date specified. ");
		}
		try {
			mProcessingDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[0]);
			Application.beginUnitOfWork();
			CalendarUtils.clearTime(mProcessingDate);
		} finally {
			Application.rollbackUnitOfWork();
		}
	}
}
