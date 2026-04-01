package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.IRSDepositFrequencyFileProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/30/12
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "IRSDepositFrequencyFileProcessorMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class IRSDepositFrequencyFileProcessorMonitor extends JSSBatchJobMonitor {
    public IRSDepositFrequencyFileProcessorMonitor(String[] pArguments) {
        super(pArguments);
    }
    public IRSDepositFrequencyFileProcessorMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }
        @Override
        public BatchJobType getBatchJobToMonitor() {
            return BatchJobType.IRSDepositFrequencyFileProcessor;
        }

        @Override
        public Class<?> getBatchJobActionToMonitor() {
            return IRSDepositFrequencyFileProcessor.ProcessDepositFrequencyFile.class;
        }
}
