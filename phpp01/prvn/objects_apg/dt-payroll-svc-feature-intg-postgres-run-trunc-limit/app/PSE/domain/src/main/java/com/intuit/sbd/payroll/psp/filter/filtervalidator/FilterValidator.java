package com.intuit.sbd.payroll.psp.filter.filtervalidator;

import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbg.psp.dbtelemetry.WorkflowFinder;
import com.intuit.sbg.psp.dbtelemetry.utils.SqlHelperUtils;
import com.intuit.sbg.psp.filtervalidator.service.FilterValidatorService;
import com.intuit.sbg.psp.sqlparser.model.QueryInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

@Slf4j
public class FilterValidator implements Callable<Void> {

    private String sql;
    private String stackTrace;
    private RequestContext requestContext;
    private Map<String, String> mdcMap;
    private FilterValidatorService filterValidatorService;
    private WorkflowFinder workflowFinder;
    private FilterValidatorCache filterValidatorCache;

    public FilterValidator(String sql, String stackTrace, RequestContext requestContext, Map<String, String> mdcMap,
                           FilterValidatorService filterValidatorService, WorkflowFinder workflowFinder, FilterValidatorCache filterValidatorCache) {
        this.sql = sql;
        this.stackTrace = stackTrace;
        this.requestContext = requestContext;
        this.mdcMap = mdcMap;
        this.filterValidatorService = filterValidatorService;
        this.workflowFinder = workflowFinder;
        this.filterValidatorCache = filterValidatorCache;
    }

    @Override
    public Void call() throws Exception {
        try {
            sql = SqlHelperUtils.sanitizeSQL(sql);
            MDC.setContextMap(mdcMap);
            String workflow = "";
            if(Objects.nonNull(workflowFinder)) {
                workflow = workflowFinder.getWorkflowName(stackTrace);
            }

            String sqlAndWorkflow = sql + workflow;
            int sqlAndWorkflowHash = sqlAndWorkflow.hashCode();

            if(filterValidatorCache.contains(sqlAndWorkflowHash)) {
                return null;
            }

            filterValidatorCache.addToCache(sqlAndWorkflowHash);

            QueryInfo queryInfo = filterValidatorService.validate(sql);
            if (Objects.isNull(queryInfo)) {
                return null;
            }

            boolean isRequestContextSet = true;
            if(!isRequestContextSet()) {
                isRequestContextSet = false;
            }

            if (!queryInfo.isPartitionKeyPresent()) {
                log.info("Event=PartitionKeyNotPresent Workflow={} RequestContextSet={} QueryInfo={}", workflow, isRequestContextSet, queryInfo);
            }
        } catch (Exception e) {
            log.error("Exception occurred during sql parsing", e);
        }
        return null;
    }

    private boolean isRequestContextSet() {
        if (Objects.isNull(requestContext) ||
                Objects.isNull(requestContext.getCompanyInfo()) ||
                Objects.isNull(requestContext.getCompanyInfo().getCompanySequence())) {
            return false;
        }
        return true;
    }

}
