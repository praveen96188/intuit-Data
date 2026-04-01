package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage.SyncAssistedUsageData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

@ScheduledJob(name = "AssistedUsageDataSyncProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class AssistedUsageDataSyncProcessor extends JSSBatchJob {
    static final SpcfLogger logger = SpcfLogManager.getLogger(AssistedUsageDataSyncProcessor.class);

    public AssistedUsageDataSyncProcessor(String[] pArguments) {
        super(pArguments);
    }
    public AssistedUsageDataSyncProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        executeStep(ProcessAssistedUsagePayrollRunEvents.class);
    }

    public static class ProcessAssistedUsagePayrollRunEvents extends JSSBatchJobStep<AssistedUsageDataSyncProcessor> {
        @Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AssistedUsageDataSyncProcess));
                new SyncAssistedUsageData().sync();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessAssistedUsagePayrollRunEvents ", t);
            }
        }
    }
}


