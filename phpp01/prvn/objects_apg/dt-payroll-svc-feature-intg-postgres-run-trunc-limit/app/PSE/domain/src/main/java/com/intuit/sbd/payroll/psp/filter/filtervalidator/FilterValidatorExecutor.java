package com.intuit.sbd.payroll.psp.filter.filtervalidator;

import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.dbtelemetry.WorkflowFinder;
import com.intuit.sbg.psp.dbtelemetry.utils.StackTraceUtils;
import com.intuit.sbg.psp.filtervalidator.service.FilterValidatorService;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class FilterValidatorExecutor {
    private ExecutorService executor;
    private FilterValidatorService filterValidatorService;
    private WorkflowFinder workflowFinder;
    private FilterValidatorCache filterValidatorCache;
    private PSPRequestContextManager pspRequestContextManager;

    @Autowired
    public FilterValidatorExecutor(FilterValidatorService filterValidatorService, @Autowired(required = false) WorkflowFinder workflowFinder, FilterValidatorCache filterValidatorCache, PSPRequestContextManager pspRequestContextManager) {
        this.filterValidatorService = filterValidatorService;
        this.workflowFinder = workflowFinder;
        this.filterValidatorCache = filterValidatorCache;
        this.pspRequestContextManager = pspRequestContextManager;
    }

    @PostConstruct
    public void init() {
        BlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>(
                500);
        executor = new ThreadPoolExecutor(10, 10, 0,
                TimeUnit.SECONDS, linkedBlockingQueue,
                new ThreadPoolExecutor.AbortPolicy());

    }

    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }

    public void parseSQL(String sql) {
        boolean isFilterValidatorEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_FILTER_VALIDATOR_ENABLED, false);
        if(!isFilterValidatorEnabled) {
            return;
        }
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        String stackTrace = StackTraceUtils.getStackTrace(new Throwable(), null, -1);
        RequestContext requestContext = pspRequestContextManager.getRequestContext();

        FilterValidator filterValidator = new FilterValidator(sql, stackTrace, requestContext, mdcMap, filterValidatorService, workflowFinder, filterValidatorCache);
        executor.submit(filterValidator);
    }
}
