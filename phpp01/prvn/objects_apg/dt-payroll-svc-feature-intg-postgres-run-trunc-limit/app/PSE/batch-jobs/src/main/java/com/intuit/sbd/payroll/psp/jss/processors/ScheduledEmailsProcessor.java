package com.intuit.sbd.payroll.psp.jss.processors;

import java.util.List;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 4/5/17
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "ScheduledEmails", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ScheduledEmailsProcessor extends JSSBatchJob {
    private SpcfCalendar mRunDate;

    public ScheduledEmailsProcessor(String[] pArguments) {
		super(pArguments);
	}

	public ScheduledEmailsProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}



    public SpcfCalendar getRunDate() {
        return mRunDate;
    }

    public void setRunDate(SpcfCalendar pRunDate) {
        mRunDate = pRunDate;
    }

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        String commandLine = getJobInstanceParameters().trim();

        if ((commandLine.length() == 0)) {
            setRunDate(now.copy());
        } else {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar parsedDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                        setRunDate(SpcfCalendar.createInstance(parsedDate.getYear(),
                                                               parsedDate.getMonth(),
                                                               parsedDate.getDay(),
                                                               SpcfTimeZone.getLocalTimeZone()));
                    }
                }
            }
        }
    }

    @Override
    protected void validateStepRuntimeParameters(String stepName) {
        validateRuntimeParameters();
    }

    @Override
    public void execute() {
    	getLogger().info("Started Scheduled Emails Processor Batch Job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(FireUsageBillingDetailsHowToEmailStep.class);
        //executeStep(new FireUsageBillingMidTrialEmailStep());
        getLogger().info("Completed Scheduled Emails Processor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class FireUsageBillingDetailsHowToEmailStep extends JSSBatchJobStep<ScheduledEmailsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ScheduledEmails);
                try {
                    PayrollServices.beginUnitOfWork();
                    createEntitlementUnitEvents(25, EventTypeCode.UsageBilling25DaysIntoSubscription);
                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step FireUsageBillingDetailsHowToEmailStep ", t);
            }
        }
    }

    public static class FireUsageBillingMidTrialEmailStep extends JSSBatchJobStep<ScheduledEmailsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ScheduledEmails);
                try {
                    PayrollServices.beginUnitOfWork();
                    createEntitlementUnitEvents(15, EventTypeCode.UsageBilling15DaysIntoSubscription);
                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step FireUsageBillingMidTrialEmailStep ", t);
            }
        }
    }

    private static void createEntitlementUnitEvents(int pDaysToSubtract, EventTypeCode pEventTypeCode) {
        SpcfCalendar c = PSPDate.getPSPTime();
        c.addDays(-pDaysToSubtract);
        CalendarUtils.clearTime(c);
        List<EntitlementUnit> entitlementUnitList = EntitlementUnit.findEntitlementUnitsOnUsageBillingBySubscriptionStartDate(c.toUtc());

        if (entitlementUnitList != null) {
            for (EntitlementUnit entitlementUnit : entitlementUnitList) {
                CompanyEvent.createUsageBillingSubscriptionEvent(entitlementUnit, pEventTypeCode);
            }
        }
    }

    public static void main(String[] args) {
    	try{
        BatchJobManager.runJob(BatchJobType.ScheduledEmails.name());
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
}
