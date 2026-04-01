package com.intuit.sbd.payroll.psp.batchjobs.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.BillingDetail;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyOffering;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.OfferingCode;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.GenerateLiabilityCheckCore;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/3/12
 * Time: 4:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessMonthlyFeesTask implements Callable<ProcessMonthlyFeesTask> {
    private static final SpcfLogger logger = Application.getLogger(ProcessMonthlyFeesTask.class);

    private final SpcfUniqueId mCompanyId;      // The company id to bill
    private final SpcfCalendar mBillingPeriod;  // The billing period (month) for this billing cycle

    private Company mCompany;
    private OfferingCode mOfferingCode;
    private CompanyBankAccount mCompanyBankAccount;

    private PSPRequestContextManager pspRequestContextManager;

    //
    //    A customer is selected for the billing of the monthly inactive fee when:
    //    - He is in an Active status
    //    - He is not on hold
    //    - He did not run a payroll dated for the previous month
    //
    //    After selecting a company with the above criteria, there is a date comparison is done to keep from billing a customer who just came on service.
    //    1) The earliest date the customer is eligible for billing is calculated by taking the later of either the middle of the customer�s first tax
    //       quarter or 30 calendar days after they sent their balance file.
    //    2) The date determined in 1) above is then compared to a calculated Process Date, which is the 30th day of the previous month � the month the
    //       batch job is running for.   If the date from 1) is less than the Process Date, then the customer is billed for the inactive fee.
    //
    //    For 1) above:
    //    � If the customer�s First Tax Qtr is Q1 the calculated middle of the qtr is 2/15
    //    � If the First Tax Qtr is Q2, the middle is 5/15
    //    � If the First Tax Qtr is Q3, the middle is 8/15
    //    � If the First Tax Qtr is Q4, the middle is 11/15
    //
    //    Example 1:
    //    � Customer sends their balance file on 6/29/12 with no payroll for June, the First Tax Qtr is 2/2012 so the middle of that is 5/15/2012.
    //      Thirty days after the balance file is 7/29/12, so this date is the later of the two dates.
    //    � A July batch job would calculate a Process Date of 6/30/12.  So, 7/29/12 is greater than 6/30/12 and this customer would not be billed.
    //
    //    Example 2:
    //    � Customer sends their balance file on 5/29/12  with a May payroll.  They did not send a payroll for June.  The First Tax Qtr is 2/2012
    //      so the middle of that is 5/15/2012.  Thirty days after the balance file is 6/29/12, so this date is the later of the two dates.
    //    � A July batch job would calculate a Process Date of 6/30/12.  And 6/29/12 is less than 6/30/12 so this customer would be billed.
    //
    //    Example 3:
    //    � Customer sends their balance file on 5/31/12  with no payroll.  The First Tax Qtr is 2/2012 so the middle of that is 5/15/2012.
    //      Thirty days after the balance file is 6/30/12, so this date is the later of the two dates.
    //    � A July batch job would calculate a Process Date of 6/30/12.  The Earliest Bill date of 6/30/12 is not less than the Process Date of
    //      6/30/12 so this customer would not be billed.
    //

    public ProcessMonthlyFeesTask(String pCompanyId, SpcfCalendar pBillingPeriod) {
        mCompanyId = SpcfUniqueId.createInstance(pCompanyId);
        mBillingPeriod = pBillingPeriod.copy();
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    private SpcfCalendar getBillingReferenceDate() {
        //
        // The Billing Reference Date is always the last day of the billing month
        //

        SpcfCalendar billingReferenceDate = CalendarUtils.getLastDayOfMonth(mBillingPeriod);

        CalendarUtils.clearTime(billingReferenceDate);

        return billingReferenceDate;
    }

    private SpcfCalendar getPaycheckDate() {
        //
        // We want Paycheck Date to be last day of the month of the Billing Reference Date
        //
        SpcfCalendar lastDayOfMonth = CalendarUtils.getLastDayOfMonth(getBillingReferenceDate());
        CalendarUtils.clearTime(lastDayOfMonth);
        return lastDayOfMonth;
    }

    private SpcfCalendar getEarliestBillingDate(SpcfCalendar pBalfDate) {
        //
        // We're guaranteed (by the caller) that pBalfDate will not be null here
        //

        // Middle of Q1 is 2/15
        // Middle of Q2 is 5/15
        // Middle of Q3 is 8/15
        // Middle of Q4 is 11/15

        int month = (CalendarUtils.getQuarterAsInt(pBalfDate) * 3) - 1;
        SpcfCalendar midQuarterDate = SpcfCalendar.createInstance(pBalfDate.getYear(), month, 15);
        SpcfCalendar balfPlus30Days = pBalfDate.copy();

        balfPlus30Days.addDays(30);

        CalendarUtils.clearTime(midQuarterDate);
        CalendarUtils.clearTime(balfPlus30Days);

        //
        // The earliest billing date is the later of midQuarterDate or balfPlus30Days
        //

        return midQuarterDate.after(balfPlus30Days) ? midQuarterDate : balfPlus30Days;
    }

    private boolean isEligibleForBilling() {
        Expression<Company> query = new Query<Company>().Where(Company.Id().equalTo(mCompanyId))
                                                        .EagerLoad(Company.CompanyServiceSet(),
                                                                   Company.CompanyOfferingSet(),
                                                                   Company.CompanyBankAccountSet(),
                                                                   Company.OnHoldReasonSet());

        DomainEntitySet<Company> companySet = Application.find(Company.class, query);

        //
        // If we can't find the company, we're done
        //
        if (companySet.isEmpty()) {
            logger.error(String.format("Unable to find company with id %s in database from ProcessMonthlyFeesTask.", mCompanyId));
            return false;
        }

        mCompany = companySet.get(0);

        //
        // If company is not Active on Tax Service, do not bill
        //
        CompanyService tax = mCompany.getService(ServiceCode.Tax);
        if ((tax == null) || !tax.isActive()) {
            logger.info(String.format("Skipping Monthly Fee assessment for company id %s in ProcessMonthlyFeesTask (company not on tax service).", mCompanyId));
            return false;
        }

        //
        // If company has never BALFed, do not bill
        //
        DomainEntitySet<CompanyEvent> eventSet = CompanyEvent.findCompanyEvents(mCompany, EventTypeCode.BalanceFileReceived).sort(CompanyEvent.EventTimeStamp());
        if (eventSet.isEmpty()) {
            logger.info(String.format("Skipping Monthly Fee assessment for company id %s in ProcessMonthlyFeesTask (no BALF).", mCompanyId));
            return false;
        }

        //
        // If never BALFed on currently active Tax service, do not bill
        //
        SpcfCalendar balfDate = null;
        String taxServiceId = tax.getId().toString();
        for (CompanyEvent balfEvent : eventSet) {
            try {
                pspRequestContextManager.setRequestContext(balfEvent.getCompany(), RequestType.OLAP, "MonthlyFee");
                String balfServiceId = balfEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyServiceId);

                if (taxServiceId.equals(balfServiceId)) {
                    balfDate = balfEvent.getEventTimeStamp();
                    break;
                }
            } finally {
                pspRequestContextManager.clearRequestContext();
            }
        }

        if (balfDate == null) {
            logger.info(String.format("Skipping Monthly Fee assessment for company id %s in ProcessMonthlyFeesTask (no BALF on current Tax service %s).",
                                      mCompanyId, taxServiceId));
            return false;
        }

        //
        // Billing Reference Date = Last day of the billing month
        // Earliest Billing Date  = Earliest date that the client can be assessed the Monthly Fee in this job
        // If the Billing Reference Date is <= Earliest Billing Date, do not bill
        //
        SpcfCalendar billingReferenceDate = getBillingReferenceDate();
        SpcfCalendar earliestBillingDate = getEarliestBillingDate(balfDate);
        if (billingReferenceDate.compareTo(earliestBillingDate) <= 0) {
            logger.info(String.format("Skipping Monthly Fee assessment for company id %s in ProcessMonthlyFeesTask (billing ref date: %s, earliest billing date for company: %s).",
                                      mCompanyId, billingReferenceDate.format("MM/dd/yyyy"), earliestBillingDate.format("MM/dd/yyyy")));
            return false;
        }

        //
        // If no DD Company Offering, do not bill
        //
        CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);

        if (companyOffering == null) {
            logger.error(String.format("Unable to retrieve DirectDeposit offering for company id %s in ProcessMonthlyFeesTask.", mCompanyId));
            return false;
        }

        //
        // If no active CBA, do not bill
        //
        mCompanyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

        if (mCompanyBankAccount == null) {
            logger.error(String.format("Unable to retrieve active company bank account for company id %s in ProcessMonthlyFeesTask.", mCompanyId));
            return false;
        }

        mOfferingCode = companyOffering.getOffering().getOfferingCode();

        return true;
    }
    
    public ProcessMonthlyFeesTask call() throws Exception {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.MonthlyFeeBatchJob);
            Application.beginUnitOfWork();

            if (isEligibleForBilling()) {
                pspRequestContextManager.setRequestContext(mCompany, RequestType.OLAP, "MonthlyFee");
                PayrollRun payrollRun = PayrollRun.createFeePayrollRun(mCompany, getPaycheckDate());

                //
                // Create fee with next available settlement date
                //
                BillingDetail.createMonthlyFeeForPayrollRunIfMeetsCriteria(payrollRun, mCompanyBankAccount, null, mOfferingCode, true);

                GenerateLiabilityCheckCore generateLiabilityCheckCore = new GenerateLiabilityCheckCore(mCompany, payrollRun);
                ProcessResult processResult = generateLiabilityCheckCore.execute();
                if(!processResult.isSuccess()) {
                    logger.error("Could not create liability check for monthly billing transaction. Company " + mCompany.getSourceSystemCompanyId() + "\nError: " + processResult.toString());
                }
               CompanyEvent.createMonthlyFeeCreatedEvent(payrollRun);
            }

            Application.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error(String.format("Unexpected exception in ProcessMonthlyFeesTask for company id %s: ", mCompanyId), t);
        } finally {
            Application.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
        
        return this;
    }
}
