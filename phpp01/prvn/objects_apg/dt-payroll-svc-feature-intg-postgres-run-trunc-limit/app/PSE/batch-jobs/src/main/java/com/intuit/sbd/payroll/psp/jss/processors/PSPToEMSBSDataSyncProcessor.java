package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.billing.SyncPSPToEMSBS;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/19/12
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "PSPToEMSBSDataSyncProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class PSPToEMSBSDataSyncProcessor extends JSSBatchJob {
	static final SpcfLogger logger = SpcfLogManager.getLogger(PSPToEMSBSDataSyncProcessor.class);

    public PSPToEMSBSDataSyncProcessor(String[] pArguments) {
            super(pArguments);
        }
    public PSPToEMSBSDataSyncProcessor(String[] pArguments, String pJobId) {
            super(pArguments, pJobId);
        }

	@Override
	protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
		executeStep(ProcessPSPToEMSBSPayrollRunEvents.class);
		executeStep(ProcessPSPToEMSBSFailedPayrollRunEvents.class);
	}

	public static class ProcessPSPToEMSBSPayrollRunEvents extends JSSBatchJobStep<PSPToEMSBSDataSyncProcessor> {
		@Override
		public void execute() {
			try {
				PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.PSPToEMSBSDataSyncBatchJob));

				new SyncPSPToEMSBS().sync();
			} catch (Throwable t) {
				throw new RuntimeException("Exception in job step ProcessPSPToEMSBSPayrollRunEvents ", t);
			}
		}
	}

	public static class ProcessPSPToEMSBSFailedPayrollRunEvents extends JSSBatchJobStep<PSPToEMSBSDataSyncProcessor> {
		@Override
		public void execute() {
			try {
				PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.PSPToEMSBSDataSyncBatchJob));

				new SyncPSPToEMSBS().syncFailed();
			} catch (Throwable t) {
				throw new RuntimeException("Exception in job step ProcessPSPToEMSBSPayrollRunEvents ", t);
			}
		}
	}

    /*
    public static void main(String[] args) {
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "2", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
    }
    */
}

