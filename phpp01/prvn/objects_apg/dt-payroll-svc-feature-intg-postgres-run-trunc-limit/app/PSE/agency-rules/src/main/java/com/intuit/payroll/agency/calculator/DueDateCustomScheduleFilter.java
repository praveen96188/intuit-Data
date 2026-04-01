//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.UserFrequencyType;
import com.intuit.payroll.agency.impl.PaymentPeriod;
import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.portability.SpcfRuntimeException;
/// <summary>
/// DueDateFormatFilter ensures the formatting of
/// payment period fields is correct.
/// </summary>
class DueDateCustomScheduleFilter extends DueDateFilter
{
	/// <summary>
	/// Default constructor.
	/// </summary>
	public DueDateCustomScheduleFilter()
	{}
	
	/// <summary>
	/// Continuation constructor. Used when there are
	/// more filters after this to assign.
	/// </summary>
	/// <param name="filter">The next filter in the chain.</param>
	public DueDateCustomScheduleFilter(DueDateFilter filter)
	{
		m_nextFilter = filter;
	}

	/// <summary>
	/// This filter is used to do any final formatting of the 
	/// fields in the payment period.
	/// </summary>
	/// <param name="request">The PaymentPeriodRequest containing
	/// the criteria for the payment period to obtain.</param>
	/// <param name="response">The payment period to return, which
	/// could be in any state depending on it's place in the chain.</param>
	public PaymentPeriod execute(IPaymentPeriodRequest request, PaymentPeriod response)
	{
		response = calculateCustomPaymentPeriod(request, response);
		return executeNext(request, response);
	}

	/// <summary>
	/// Uses the custom params to create a payment period.
	/// </summary>
	/// <param name="paymentPeriodRq"></param>
	/// <param name="response"></param>
	protected PaymentPeriod calculateCustomPaymentPeriod(IPaymentPeriodRequest paymentPeriodRq, PaymentPeriod response)
	{
		// We now have to apply the custom due date calcs since the UserDefinedRules.xml 
		// cannot know what the user chose for this particular template.  This is defined in the 
		// UserScheduleNum.
        UserFrequencyType userFreq = paymentPeriodRq.getUserFrequency();
        if (userFreq == UserFrequencyType.CustomAnnual)
        {
			RulesCalendar dueDate = response.getToAccrualDate();
            if (paymentPeriodRq.getUserScheduleDate() != null) {
	            // Take into account the UserScheduleDate and apply it directly, keep the year from the calculated date.
	            dueDate = RulesCalendar.createCalendar(response.getDueDate().getYear(),
	                    paymentPeriodRq.getUserScheduleDate().getMonth(),
	                    paymentPeriodRq.getUserScheduleDate().getDay()) ;
			}
	        response.setDueDate(dueDate);
        }
        else if (userFreq == UserFrequencyType.CustomQuarterly ||
                userFreq == UserFrequencyType.CustomMonthly)
        {
            int day = paymentPeriodRq.getUserScheduleNum();
            int daysInMonth = response.getDueDate().getDaysInMonth();

            // Special handling for folks who set things to EOM.
            if (day > daysInMonth)
            {
                // Set it to the last day of the month, taking into account leap year etc...
                day = daysInMonth;
            }

			// 0 means end of period, >0 means offset into next period
			RulesCalendar dueDate = response.getToAccrualDate();
			if (day > 0) {
	            dueDate = RulesCalendar.createCalendar(
	                    response.getDueDate().getYear(),
	                    response.getDueDate().getMonth(),
	                    day
	            );
			} 

            response.setDueDate(dueDate);
        }
				// If it's Weekly the number refers to the day of the week after the end of the period.
        else if (userFreq == UserFrequencyType.CustomWeekly)
        {
            // We're in the right week, just need to adjust to the day.
            int tempDOW = response.getDueDate().getDayOfWeek();
            int diff = paymentPeriodRq.getUserScheduleNum() - tempDOW;

            RulesCalendar dueDate = response.getDueDate().addDays(diff);
            if (diff < 0)
            {
                // roll to next week
                dueDate = dueDate.addWeeks(1);
            }
            response.setDueDate(dueDate);
        }
        else if (userFreq == UserFrequencyType.AfterEveryPayrollRun)
        {
			RulesCalendar accrualDate = paymentPeriodRq.getAccrualDate();
			RulesCalendar dueDate = accrualDate.addDays(paymentPeriodRq.getUserScheduleNum());
			response.setDueDate(dueDate);

        }
        else
        {

            throw new RuntimeException("unknown user frequency");
//            throw new SpcfRuntimeException("unknown user frequency");
		}
		return response;// we didn't create a new response, we just mutated some of its contents
	}
}
