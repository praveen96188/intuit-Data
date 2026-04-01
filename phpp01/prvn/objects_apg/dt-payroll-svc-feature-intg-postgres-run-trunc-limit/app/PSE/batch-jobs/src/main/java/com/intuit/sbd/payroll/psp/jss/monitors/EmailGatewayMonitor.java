package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.EmailGatewayProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: May 4, 2017
 * Time: 3:38:01 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name="EmailGatewayMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EmailGatewayMonitor extends JSSBatchJobMonitor{
	   public EmailGatewayMonitor(String[] pArguments) {
	        super(pArguments);
	        setWarnOnMultipleAuditEntries(Boolean.FALSE);
	    }

	    public EmailGatewayMonitor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	        setWarnOnMultipleAuditEntries(Boolean.FALSE);
	    }
	    
	    @Override
	    public BatchJobType getBatchJobToMonitor() {
	        return BatchJobType.EmailGateway;
	    }
	    @Override
	    public Class<?> getBatchJobActionToMonitor() {
	        return EmailGatewayProcessor.ProcessEmails.class;
	    }
       
}
