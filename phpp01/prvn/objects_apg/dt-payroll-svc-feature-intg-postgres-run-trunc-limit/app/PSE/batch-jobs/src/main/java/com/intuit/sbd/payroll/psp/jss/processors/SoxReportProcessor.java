package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain.SoxDataManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.service.SoxAppUserService;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.service.SoxDBUserService;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.service.SoxUserService;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

@ScheduledJob(name = "SoxReport", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class SoxReportProcessor extends JSSBatchJob {

    public SoxReportProcessor(String[] pArguments) {
        super(pArguments);
    }

    public SoxReportProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() throws Exception {
        getLogger().info("Event=SoxReportBatchJob SubEvent=Starting_SoxReportProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(SoxDBReportProcessingStep.class);
        executeStep(SoxAppReportProcessingStep.class);

        getLogger().info("Event=SoxReportBatchJob SubEvent=Completed_SoxReportProcessor batch job. Elapsed_time={}", timer.stop().getElapsedTimeString());
    }

    public static class SoxDBReportProcessingStep extends JSSBatchJobStep<SoxReportProcessor> {
        private SoxUserService soxDbUserService;

        public SoxDBReportProcessingStep() {
            soxDbUserService = PayrollApplicationBeanFactory.getBean(SoxDBUserService.class);
        }

        @Override
        protected void execute() throws Exception {
            try {
                getLogger().info("Event=SoxReportBatchJob SubEvent=Executing_SoxDBReportProcessingStep..");

                getLogger().info("Event=SoxReportBatchJob SubEvent=Executing_DBReport_Monolith.");
                StopWatch timer = StopWatch.startTimer();
                soxDbUserService.execute(BatchJobConstants.SOX_RETRY_COUNT,SoxDataManager.DATA_MANAGER_DB_MONOLITH);
                getLogger().info("Event=SoxReportBatchJob SubEvent=Completed_DBReport_Monolith. Elapsed_time={}", timer.stop().getElapsedTimeString());

                getLogger().info("Event=SoxReportBatchJob SubEvent=Executing_DBReport_Audit.");
                timer = StopWatch.startTimer();
                soxDbUserService.execute(BatchJobConstants.SOX_RETRY_COUNT,SoxDataManager.DATA_MANAGER_DB_AUDIT);
                getLogger().info("Event=SoxReportBatchJob SubEvent=Completed_DBReport_Audit. Elapsed_time={}", timer.stop().getElapsedTimeString());

                getLogger().info("Event=SoxReportBatchJob SubEvent=Completed_SoxDBReportProcessingStep.");
            } catch (Exception e) {
                getLogger().error("Event=SoxReportBatchJobError : SoxDBReportProcessingStep threw an exception: ", e);
                throw e;
            }
        }
    }

    public static class SoxAppReportProcessingStep extends JSSBatchJobStep<SoxReportProcessor> {
        private SoxUserService soxAppUserService;

        public SoxAppReportProcessingStep() {
            soxAppUserService = PayrollApplicationBeanFactory.getBean(SoxAppUserService.class);
        }

        @Override
        protected void execute() throws Exception {
            try {
                getLogger().info("Event=SoxReportBatchJob: SubEvent=Executing_SoxAppReportProcessingStep..");
                StopWatch timer = StopWatch.startTimer();

                soxAppUserService.execute(BatchJobConstants.SOX_RETRY_COUNT, SoxDataManager.DATA_MANAGER_APP);

                getLogger().info("Event=SoxReportBatchJob: SubEvent=Completed_SoxAppReportProcessingStep. Elapsed_time={}", timer.stop().getElapsedTimeString());
            }
            catch(Exception e) {
                getLogger().error("Event=SoxReportBatchJobError: SoxAppReportProcessingStep threw an exception: ",e);
                throw e;
            }
        }
    }
}







