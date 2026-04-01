package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.entity.*;
import com.intuit.sbd.payroll.psp.batchjobs.entity.CompanyPublisherStrategy;
import com.intuit.sbd.payroll.psp.batchjobs.entity.company.CompanyFindStrategy;
import com.intuit.sbd.payroll.psp.batchjobs.entity.company.CompanyUpdateStrategy;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intent for this job is to process Entity publishing into the
 * internal Kafka Event Buss Topic.
 */
@ScheduledJob(name = "EntityInitialLoadProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EntityInitialLoadProcessor extends JSSBatchJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityInitialLoadProcessor.class);

    public EntityInitialLoadProcessor(String[] pArguments) {
        super(pArguments);
    }

    public EntityInitialLoadProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() throws Exception {
        LOGGER.info("job=initial_load,action=execute_starting");

        StopWatch timer = StopWatch.startTimer();

        executeStep(CompanyEntityProcessor.class);

        LOGGER.info("job=initial_load,Completed " + getClass().getSimpleName() + ". ElapsedTime=" + timer.stop().getElapsedMillis());
    }

    public static class CompanyEntityProcessor extends JSSBatchJobStep<EntityInitialLoadProcessor> {

        @Override
        protected void execute() throws Exception {
            LOGGER.info("job=initial_load,action=execute_starting,step");
            try {
                String pCommandLineArg = getBatchJobProcessor().getJobInstanceParameters().trim();
                getLogger().info("job=initial_load,Command Line Arguments: " + pCommandLineArg);

                String[] args = null;

                if (pCommandLineArg.trim().length() > 0) {
                    args = pCommandLineArg.split(" ");
                }

                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EntityInitialLoadProcessor));

                EntityPublisherConfig entityPublisherConfig = new EntityPublisherConfig(args);

                EventPublisher<Object[]> companyEventPublisher = new DefaultEventPublisher<>(
                        new CompanyFindStrategy(entityPublisherConfig.getBatchSize(), entityPublisherConfig.getCompanyPublishStatusWorkflows(), entityPublisherConfig.getPublishStatusWorkflowState(), entityPublisherConfig.getSourceCompanyIds(), entityPublisherConfig.getNamedQuery()),
                        new CompanyUpdateStrategy(entityPublisherConfig.getCompanyPublishStatusWorkflows()),
                        new DefaultPartitionStrategy(entityPublisherConfig.getChunkSize()),
                        new CompanyPublisherStrategy(entityPublisherConfig.getTopicName(),entityPublisherConfig.getCompanyPublishStatusWorkflows(), entityPublisherConfig.isRepublishMode()));

                companyEventPublisher.publish();

            } catch (Exception ex) {
                LOGGER.error("job=initial_load,Failed to execute the CompanyEntityProcessor step ", ex);
            }
            LOGGER.info("job=initial_load,action=execute_completed,step");
        }
    }
}