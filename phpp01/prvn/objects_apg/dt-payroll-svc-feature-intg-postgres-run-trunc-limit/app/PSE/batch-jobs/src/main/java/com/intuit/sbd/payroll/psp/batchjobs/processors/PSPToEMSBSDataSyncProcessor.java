package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.billing.SyncPSPToEMSBS;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/19/12
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class PSPToEMSBSDataSyncProcessor extends BatchJobProcessor {
	static final SpcfLogger logger = SpcfLogManager.getLogger(PSPToEMSBSDataSyncProcessor.class);

	public PSPToEMSBSDataSyncProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
		super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
	}

	@Override
	protected void execute() {
		logger.info("Starting " + getClass().getSimpleName() + " process job");
		executeStep(new ProcessPSPToEMSBSPayrollRunEvents());
		executeStep(new ProcessPSPToEMSBSFailedPayrollRunEvents());
	}

	public class ProcessPSPToEMSBSPayrollRunEvents extends BatchJobProcessorStep {
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

	public class ProcessPSPToEMSBSFailedPayrollRunEvents extends BatchJobProcessorStep {
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

