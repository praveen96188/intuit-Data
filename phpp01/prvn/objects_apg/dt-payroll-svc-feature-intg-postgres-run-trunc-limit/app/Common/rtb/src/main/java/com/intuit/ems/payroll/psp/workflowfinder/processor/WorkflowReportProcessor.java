package com.intuit.ems.payroll.psp.workflowfinder.processor;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intuit.ems.payroll.psp.config.WorkflowModelConfig;
import com.intuit.ems.payroll.psp.workflowfinder.model.WorkflowModel;
import com.intuit.ems.payroll.psp.workflowfinder.service.WorkflowFinderService;

/**
 * @author rn5
 *
 */
@Component
public class WorkflowReportProcessor {

    private WorkflowModelConfig workflowModelConfig;
    private WorkflowFinderService workflowFinderService;
    private static final SpcfLogger LOGGER = Application.getLogger(WorkflowReportProcessor.class);

    @Autowired
    public WorkflowReportProcessor(WorkflowModelConfig workflowModelConfig,
                                   WorkflowFinderService workflowFinderService) {
        this.workflowModelConfig = workflowModelConfig;
        this.workflowFinderService = workflowFinderService;
    }

    public void generateWorkflowReports () throws IOException {
        for(Entry<String, List<WorkflowModel>> workflowModelEntrySet : workflowModelConfig.getModules().entrySet()) {
            String moduleName = workflowModelEntrySet.getKey();
            List<WorkflowModel> workflowModelList = workflowModelEntrySet.getValue();
            LOGGER.info("********** Start " + moduleName + " **********");
            workflowFinderService.generateReport(workflowModelList);
            LOGGER.info("********** End " + moduleName + " **********");
        }
    }
}
