package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.SalesTaxExceptionProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 10/11/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 * 
 * SalesTaxExceptionMonitor has been changed to monitor last step (SalesTaxExceptionProcessor.ReCalculateSalesTax) of the SalesTaxExceptionProcessor batch job 
 * 
 */
@ScheduledJob(name = "SalesTaxExceptionMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class SalesTaxExceptionMonitor extends JSSBatchJobMonitor {

    public SalesTaxExceptionMonitor(String[] pArguments) {
        super(pArguments);
    }
    public SalesTaxExceptionMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.SalesTaxExceptionProcessor;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return SalesTaxExceptionProcessor.ReCalculateSalesTax.class;
    }

}
