package com.intuit.ems.payroll.psp;

import com.intuit.ems.payroll.psp.workflowfinder.processor.WorkflowReportProcessor;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;

import java.io.IOException;

public class WorkflowReportGenerator {

	public static void main(String[] args) throws IOException {
		try {
			Application.initialize();
			WorkflowReportProcessor workflowReportProcessor = PayrollApplicationBeanFactory.getBean(WorkflowReportProcessor.class);
			workflowReportProcessor.generateWorkflowReports();
		} finally {
			Application.uninitialize();
		}
	}

}
