package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 21, 2010
 * Time: 1:20:15 PM
 */

@ScheduledJob(name = "EntitlementProcessorMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class,singleton = false)
public class EntitlementProcessorMonitor extends JSSBatchJobMonitor {
    	public EntitlementProcessorMonitor(String[] pArguments) {
        	super(pArguments);
			setWarnOnMultipleAuditEntries(false);
    	}
    	public EntitlementProcessorMonitor(String[] pArguments, String pJobId) {
    	        super(pArguments, pJobId);
    	        setWarnOnMultipleAuditEntries(false);
    	}
    	
    	@Override
    	public BatchJobType getBatchJobToMonitor() {
    	    return BatchJobType.EntitlementProcessor;
    	}
    	@Override
    	public Class<?> getBatchJobActionToMonitor() {
    	    return EntitlementProcessor.EntitlementDisableStep.class;
    	}
        
        
       
}
