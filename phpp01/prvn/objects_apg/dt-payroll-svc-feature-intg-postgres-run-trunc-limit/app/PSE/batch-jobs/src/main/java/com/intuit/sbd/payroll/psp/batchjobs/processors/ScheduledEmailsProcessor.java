package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 10/1/12
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledEmailsProcessor extends BatchJobProcessor {
    private SpcfCalendar mRunDate;

    public ScheduledEmailsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
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

        if ((getRunMode() == RunMode.UsingFlux) || (commandLine.length() == 0)) {
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
        logger.info("StartedUsageBillingWelcomeEmailsProcessor Batch Job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(new FireUsageBillingDetailsHowToEmailStep());
        //executeStep(new FireUsageBillingMidTrialEmailStep());
        logger.info("Completed UsageBillingWelcomeEmailsProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class FireUsageBillingDetailsHowToEmailStep extends BatchJobProcessorStep {
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

    public class FireUsageBillingMidTrialEmailStep extends BatchJobProcessorStep {
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

    private void createEntitlementUnitEvents(int pDaysToSubtract, EventTypeCode pEventTypeCode) {
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
        BatchJobManager.runJob(BatchJobType.ScheduledEmails);
    }
}
