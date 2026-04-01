package com.intuit.sbd.payroll.psp.jss;

import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.utils.CallbackDataGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback generator for all the JSS Batch Jobs
 * 
 * Its job responsibility to set the correct call back data in the ThreadLocalManagar using
 * <code>com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager.setValue(String)</code>
 * 
 * @author kmuthurangam
 *
 */
public class JSSCallBackGenerator implements CallbackDataGenerator {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public String getCallbackData(ScheduledJob scheduledJob) {
		/*
		 * Retrieving the Batch Job arguments from Thread Local, because Batch Job arguments for all the programmatically
		 * scheduled jobs are shared via Thread Context (ThreadLocal) variable.
		 */
		String callBackData = ThreadLocalManager.getValue();
		if (callBackData == null) {
			logger.warn("No batch job arguments are found for the batch job type " + scheduledJob.name());
		}
		return callBackData;
	}

}
