package com.intuit.sbd.payroll.psp.common.utils.workflowfinder;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess.WorkflowPreProcessor;
import com.intuit.sbg.psp.dbtelemetry.WorkflowFinder;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import javax.annotation.PostConstruct;
@Component
public class WorkflowFinderImpl implements WorkflowFinder {

    private WorkflowPreProcessor workflowPreProcessor;
    private Map<Integer, String> workflowDigestMap;
    private static final SpcfLogger LOGGER = Application.getLogger(WorkflowFinderImpl.class);

    @Autowired
    public WorkflowFinderImpl(WorkflowPreProcessor workflowPreProcessor) {
        this.workflowPreProcessor = workflowPreProcessor;
    }

    @PostConstruct
    public void init() {
        this.workflowDigestMap = workflowPreProcessor.getWorkflowDigestMap();
    }

    @Override
    public String getWorkflowName(String stackTrace) {
        try {
            if (workflowDigestMap.isEmpty()) {
                return StringUtils.EMPTY;
            }
            return findWorkflow(stackTrace);
        } catch (Exception e) {
            LOGGER.warn("Exception while finding the workflow " + e);
            return StringUtils.EMPTY;
        }
    }

    private String findWorkflow(String stackTrace) {
        String[] stackTraceWithLineNumbers = stackTrace.split(System.lineSeparator());
        for(String line : stackTraceWithLineNumbers) {
            if (StringUtils.isNotBlank(line)) {
                String stackTraceWithoutLineNumbers = line.substring(0, line.indexOf(WorkflowFinderConstants.LINE_NUMBER_SURROUND_CHAR));
                String finalStackTrace = stackTraceWithoutLineNumbers.replaceFirst(WorkflowFinderConstants.STACK_TRACE_AT, StringUtils.EMPTY).trim();
                Integer stackTraceDigest = finalStackTrace.hashCode();
                String expectedWorkflow = workflowDigestMap.get(stackTraceDigest);
                if (expectedWorkflow != null && expectedWorkflow.equals(finalStackTrace)) {
                    return expectedWorkflow;
                }
            }
        }
        return StringUtils.EMPTY;
    }
}