package com.intuit.sbd.payroll.psp.jss.monitors;


import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.SendMonthlyDataToTFSProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
@ScheduledJob(name = "SendMonthlyDataToTFSMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class SendMonthlyDataToTFSMonitor extends JSSBatchJobMonitor {
   
    public SendMonthlyDataToTFSMonitor(String[] pArguments) {
        super(pArguments);
    }

    public SendMonthlyDataToTFSMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }
    
    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return  SendMonthlyDataToTFSProcessor.SendMonthlyDataToTFSStep.class;
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return  BatchJobType.SendMonthlyDataToTFSProcessor;
    }

}