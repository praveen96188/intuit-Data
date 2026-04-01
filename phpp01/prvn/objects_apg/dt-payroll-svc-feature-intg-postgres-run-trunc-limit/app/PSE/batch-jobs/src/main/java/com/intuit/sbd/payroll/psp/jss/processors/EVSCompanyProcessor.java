package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.entity.*;
import com.intuit.sbd.payroll.psp.batchjobs.entity.CompanyPublisherStrategy;
import com.intuit.sbd.payroll.psp.batchjobs.entity.company.CompanyHourlyFindStrategy;
import com.intuit.sbd.payroll.psp.batchjobs.entity.company.CompanyHourlyUpdateStrategy;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intent for this job is to process Entity publishing into the
 * internal Kafka Event Buss Topic.
 */
@ScheduledJob(name = "EVSCompanyProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EVSCompanyProcessor extends JSSBatchJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(EVSCompanyProcessor.class);

    public EVSCompanyProcessor(String[] pArguments) {
        super(pArguments);
    }

    public EVSCompanyProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() throws Exception {
        LOGGER.info("job=initial_load_evs_hourly,action=execute_starting");

        StopWatch timer = StopWatch.startTimer();

        executeStep(EVSCompanyProcessor.CompanyEntityProcessor.class);

        LOGGER.info("job=initial_load_evs_hourly,action=execute_completed.elapsed_time=" + timer.stop().getElapsedMillis());
    }

    public static class CompanyEntityProcessor extends JSSBatchJobStep<EVSCompanyProcessor> {

        @Override
        protected void execute() throws Exception {
            LOGGER.info("job=initial_load_evs_hourly,action=execute_starting,step");

            String pCommandLineArg = getBatchJobProcessor().getJobInstanceParameters().trim();
            getLogger().info("job=initial_load_evs_hourly,Command Line Arguments: " + pCommandLineArg);
            String[] args = null;
            if (pCommandLineArg.trim().length() > 0) {
                args = pCommandLineArg.split(" ");
            }

            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EVSCompanyProcessor));

                EntityPublisherConfig entityPublisherConfig = new EntityPublisherConfig(args);
                entityPublisherConfig.setCompanyPublishStatusWorkflows(CompanyPublishStatusWorkflows.EVS_HOURLY);
                EventPublisher<Object[]> companyEventPublisher = new DefaultEventPublisher<>(
                        new CompanyHourlyFindStrategy(entityPublisherConfig.getBatchLastProcessed(), entityPublisherConfig.getBatchStartTime(), entityPublisherConfig.getSourceCompanyIds()),
                        new CompanyHourlyUpdateStrategy(entityPublisherConfig.getBatchStartTime()),
                        new DefaultPartitionStrategy(entityPublisherConfig.getChunkSize()),
                        new CompanyPublisherStrategy(entityPublisherConfig.getTopicName(), entityPublisherConfig.getCompanyPublishStatusWorkflows(), entityPublisherConfig.isRepublishMode()));

                companyEventPublisher.publish();

            } catch (Exception ex) {
                LOGGER.error("job=initial_load_evs_hourly,Failed to execute the EVSCompanyProcessor step ", ex);
            }
            LOGGER.info("job=initial_load_evs_hourly,action=execute_completed,step");
        }
    }
}
