package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.WorkflowFinderImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * RTB program which identifies the workflows based on the stack trace.
 *
 */
public class WorkflowFinderProcessor {
    private static final SpcfLogger LOGGER = Application.getLogger(WorkflowFinderProcessor.class);

    public static void main(String[] args) throws IOException {

        try {
            Application.initialize();
            WorkflowFinderImpl workflowFinder = PayrollApplicationBeanFactory.getBean(WorkflowFinderImpl.class);
            String stackTrace = new String(Files.readAllBytes(Paths.get("/apps/batch/jss/shell/Stacktrace.txt")));
            String workflow = workflowFinder.getWorkflowName(stackTrace);
            LOGGER.info("Workflow : " + workflow);
        } finally {
            Application.uninitialize();
        }


    }
}