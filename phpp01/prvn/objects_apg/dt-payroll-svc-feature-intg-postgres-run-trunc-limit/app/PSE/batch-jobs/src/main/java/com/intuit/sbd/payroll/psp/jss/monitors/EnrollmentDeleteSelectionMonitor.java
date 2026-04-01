package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.EnrollmentDeleteSelectionProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: dweinberg
 * Date: 4/25/13
 * Time: 12:29 PM
 *
 * Migrated To JSS by: nloharuka
 * Date: 5/03/17
 * PSP-13001
 */

@ScheduledJob(name = "EnrollmentDeleteSelectionMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EnrollmentDeleteSelectionMonitor extends JSSBatchJobMonitor {

    public EnrollmentDeleteSelectionMonitor(String[] pArguments) {
        super(pArguments);
    }
    public EnrollmentDeleteSelectionMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.EnrollmentDeleteSelectionProcessor;
    }
    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return EnrollmentDeleteSelectionProcessor.SelectRAFEnrollmentsForDelete.class;
    }

}
