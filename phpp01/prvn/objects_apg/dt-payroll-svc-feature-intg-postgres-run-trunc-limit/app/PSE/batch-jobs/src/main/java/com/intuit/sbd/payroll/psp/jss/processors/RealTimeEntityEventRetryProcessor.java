package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.entity.retry.RealTimeEntityEventRetryConfig;
import com.intuit.sbd.payroll.psp.batchjobs.entity.retry.RealTimeEntityEventRetryFetchService;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.context.threading.ChildThreadRequestContextHelper;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.entity.publisher.PublisherService;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;

@ScheduledJob(name="RealTimeEntityEventRetryProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class RealTimeEntityEventRetryProcessor extends JSSBatchJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeEntityEventRetryProcessor.class);

    public RealTimeEntityEventRetryProcessor(String[] pArguments) {
        super(pArguments);
    }

    public RealTimeEntityEventRetryProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    private RealTimeEntityEventRetryConfig realTimeEntityEventRetryConfig;
    private List<EntityUpdate> entityUpdates;
    private static final int maxRetryCount = 3;

    @Override
    protected void validateRuntimeParameters() {
        //set JSS, System parameter for configurable inputs
        setConfigParameters();

        //load entityUpdate
        fetchEntityIds();
    }

    private void setConfigParameters() {
        String pCommandLineArg = getJobInstanceParameters().trim();
        LOGGER.info("job=RealTimeEntityEventRetryProcessor, Action=Command Line Arguments={}", pCommandLineArg);

        String[] args = null;
        if (pCommandLineArg.trim().length() > 0) {
            args = pCommandLineArg.split(" ");
        }
        realTimeEntityEventRetryConfig = new RealTimeEntityEventRetryConfig(args);

        //By default, batch job will take last 11 hours records
        //Start time T-12, End time T-1
        if(Objects.isNull(realTimeEntityEventRetryConfig.getStartTime())) {
            realTimeEntityEventRetryConfig.setStartTime(PSPDate.getPSPTime());
            realTimeEntityEventRetryConfig.getStartTime().addHours(-12);
        }
        if(Objects.isNull(realTimeEntityEventRetryConfig.getEndTime())) {
            realTimeEntityEventRetryConfig.setEndTime(PSPDate.getPSPTime());
            realTimeEntityEventRetryConfig.getEndTime().addHours(-1);
        }
        if(CollectionUtils.isEmpty(realTimeEntityEventRetryConfig.getEntityNamesToPublish())) {
            String entityEnabledForRepublish = ConfigurationManager.getSettingValue(ConfigurationModule.Common,
                    "ff_entity_enabled_for_publish");
            realTimeEntityEventRetryConfig.setEntityNamesToPublish(Arrays.asList(entityEnabledForRepublish.split(",")));
        }

        LOGGER.info("job=RealTimeEntityEventRetryProcessor Parameter={}", realTimeEntityEventRetryConfig);
    }

    //If parameter for entityIds is passed in JSS parameter, then it will retry for entityIds passed
    //Else it will query the database to get all eligible entities
    private void fetchEntityIds() {
        try {
            Application.beginUnitOfWork();

            if (!CollectionUtils.isEmpty(realTimeEntityEventRetryConfig.getEntityIds())) {
                entityUpdates = EntityUpdate.findEntitiesUsing(realTimeEntityEventRetryConfig.getEntityIds());
                LOGGER.info("job=RealTimeEntityEventRetryProcessor, Action=RetrievedEntity Source=JSSParameter EntitiesSize={}", entityUpdates.size());
            } else {
                entityUpdates = EntityUpdate.findEntitiesUsing(realTimeEntityEventRetryConfig.getStartTime(),
                        realTimeEntityEventRetryConfig.getEndTime(), realTimeEntityEventRetryConfig.getStatuses(),
                        realTimeEntityEventRetryConfig.getEntityNamesToPublish(), realTimeEntityEventRetryConfig.getBatchSize(),
                        maxRetryCount);
                LOGGER.info("job=RealTimeEntityEventRetryProcessor, Action=RetrievedEntity Source=Query EntitiesSize={}", entityUpdates.size());
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Override
    protected void execute() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RealTimeRetryProcessor));

        LOGGER.info("job=RealTimeEntityEventRetryProcessor, Action=Execute_starting");

        StopWatch timer = StopWatch.startTimer();

        executeStep(RepublishFailedEntity.class);

        LOGGER.info("job=RealTimeEntityEventRetryProcessor, Action=Completed {} ElapsedTime={}", getClass().getSimpleName(), timer.stop().getElapsedMillis());
    }

    public static class RepublishFailedEntity extends JSSBatchJobStep<RealTimeEntityEventRetryProcessor> {

        private RealTimeEntityEventRetryFetchService retryFetchService = PayrollApplicationBeanFactory.getBean(RealTimeEntityEventRetryFetchService.class);

        @Override
        protected void execute() throws Exception {
            LOGGER.info("job=RealTimeEntityEventRetryProcessor, Action=execute_starting,step");

            if(CollectionUtils.isEmpty(getBatchJobProcessor().entityUpdates)) {
                LOGGER.info("job=RealTimeEntityEventRetryProcessor, Action=execute_skipped_no_records,step");
                return;
            }

            List<List<EntityUpdate>> partitionedEntityUpdateList = ListUtils.partition(getBatchJobProcessor().entityUpdates,
                    getBatchJobProcessor().realTimeEntityEventRetryConfig.getChunkSize());
            multiThreadingProcessing(partitionedEntityUpdateList);
        }

        private void multiThreadingProcessing( List<List<EntityUpdate>> partitionedEntityUpdateList) {

            int corePoolSize = Math.min(partitionedEntityUpdateList.size(), 10);
            ExecutorService executor = new ThreadPoolExecutor(corePoolSize, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            List<Future<Void>> results = new ArrayList<>();

            try {
                for (List<EntityUpdate> entityUpdates : partitionedEntityUpdateList) {
                    ChildThreadRequestContextHelper childThreadRequestContextHelper = new ChildThreadRequestContextHelper();
                    childThreadRequestContextHelper.loadThreadLocals();
                    Future<Void> result = executor.submit(
                            new PublisherService(retryFetchService.createEntityEventContext(entityUpdates), childThreadRequestContextHelper));
                    results.add(result);
                }
                for(Future<Void> result : results) {
                    try{
                        result.get();
                    } catch (Exception e) {
                        LOGGER.error("Error in thead", e);
                    }
                }
            } finally {
                if(Objects.nonNull(executor)) {
                    executor.shutdownNow();
                }
            }
        }
    }
}