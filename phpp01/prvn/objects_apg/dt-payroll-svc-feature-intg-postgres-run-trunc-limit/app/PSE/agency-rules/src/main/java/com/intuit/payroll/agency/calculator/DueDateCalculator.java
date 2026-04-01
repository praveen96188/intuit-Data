//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.UserFrequencyType;
import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.payroll.agency.api.PaymentPeriodRequestType;
import com.intuit.payroll.agency.impl.PaymentPeriod;
import com.intuit.payroll.agency.util.RulesCalendar;

/// <summary>
/// Responsible for determining Due dates for payments and forms.
/// </summary>
/// <remarks>
/// Delegates most of the business logic to Filter chains and helper classes.
/// 
/// This class is a stateless singleton and can be re-used for more than one calculation.
/// 
/// Various patterns are in effect here.  This is a singleton which mediates logic on behalf of the client code to determine 
/// due dates.  It leverages Strategy to determine a set of processing steps, and delegates those steps to filters
/// assembled in a classic Filter Chain.
/// 
/// Pattern: Singleton
/// Pattern: Filter Chain
/// Pattern: Strategy
/// Pattern: Mediator
/// </remarks>
public class DueDateCalculator
{
// We have to support mapping from an Enum to String values in the supporting rules, these
// String values are stored in the resources.
//	private static String ANNUAL_FREQUENCY = RulesConfig.getInstance().getConfigSetting("AnnualFrequency");
//	private static String QUARTERLY_FREQUENCY = RulesConfig.getInstance().getConfigSetting("QuarterlyFrequency");
//	private static String MONTHLY_FREQUENCY = RulesConfig.getInstance().getConfigSetting("MonthlyFrequency");
//	private static String WEEKLY_FREQUENCY = RulesConfig.getInstance().getConfigSetting("WeeklyFrequency");
//	private static String SEMIWEEKLY_FREQUENCY = RulesConfig.getInstance().getConfigSetting("SemiWeeklyFrequency");
//	private static String NEXTBANKINGDAY_FREQUENCY = RulesConfig.getInstance().getConfigSetting("NextBankingDayFrequency");
	private static final String ANNUAL_FREQUENCY = "ANNUAL";
	private static final String QUARTERLY_FREQUENCY = "QUARTERLY";
	private static final String MONTHLY_FREQUENCY = "MONTHLY";
	private static final String WEEKLY_FREQUENCY = "WEEKLY";
	private static final String SEMIWEEKLY_FREQUENCY = "SEMIWEEKLY";
	private static final String NEXTBANKINGDAY_FREQUENCY = "NEXTBANKINGDAY";

	private DueDateFilter m_rulesPrintPaymentChain = applyRulesPrintStrategy();
	private DueDateFilter m_rulesEpaymentChain = applyRulesEpayStrategy();
	private DueDateFilter m_userPrintPaymentChain = applyCustomPrintStrategy();
	private DueDateFilter m_userEpaymentChain = applyCustomEpayStrategy();
    private DueDateFilter m_rulesDueDateChain = applyRulesDueDateStrategy();

	/// <summary>
	/// Singleton does not allow for instantiation.
	/// </summary>
	private DueDateCalculator() {}

	/// <summary>
	/// Use this static method to get an instance.  Isn't a singleton, could be, but if so lock carefully.
	/// </summary>
	/// <returns>An instance of the Calculator.</returns>
	public static DueDateCalculator getInstance()
	{
		return new DueDateCalculator();
	}

	/// <summary>
	/// Returns a Payment Period for the request parameters supplied.
	/// </summary>
	/// <remarks>
	/// This is the pivot point for determining the how and what gets executed. 
	/// </remarks>
	/// <param name="paymentPeriodRq"></param>
	/// <returns></returns>
	public IPaymentPeriod getPaymentPeriod( IPaymentPeriodRequest paymentPeriodRq ) 
	{
		PaymentPeriodRequestType requestType = paymentPeriodRq.getRequestType();
        if (requestType == PaymentPeriodRequestType.RulesBased)
        {
            if (paymentPeriodRq.getUserFrequency() == UserFrequencyType.NoSchedule)
            {
                // This is a rules-driven calculation.
                return getRulesBasedPaymentPeriod(paymentPeriodRq);
            }
            else
            {
                // They have overridden the rules
                return getUserDefinedPaymentPeriod(paymentPeriodRq);
            }
        }
        else if (requestType == PaymentPeriodRequestType.UserDefined)
        {
			return getUserDefinedPaymentPeriod(paymentPeriodRq);
        }
        else
        {
			throw new RuntimeException("Invalid Payment Period Request Argument");
//			throw new SpcfIllegalArgumentException("Invalid Payment Period Request Argument");
		}

	}

	/// <summary>
	/// Retrieve an IPaymentPeriod for the given payment template and
	/// frequency whose date range includes the passed-in accrualDate.
	/// </summary>
	/// <remarks>
	/// Ensures that the payment period due date is correct (i.e., it 
	/// does not fall on a weekend or on a holiday observed by the agency 
	/// in question).
	/// </remarks>
	/// <param name="paymentPeriodRq">
	/// The request object containing the criteria for
	/// constructing a rules-based payment period object.
	/// </param>
	/// <returns>
	/// A payment period with a due date guaranteed to be correct based on 
	/// agency holiday and weekend rules.
	/// </returns>
	/// <exception cref="NoResultsFoundException">Thrown if no Payment period comes back.</exception>
	protected IPaymentPeriod getRulesBasedPaymentPeriod( IPaymentPeriodRequest paymentPeriodRq )
	{	
		PaymentPeriod rulesPaymentPeriod = null;
        if (paymentPeriodRq.getSubmitMethodId().equals("EFE-EPAY"))
        {
            rulesPaymentPeriod = m_rulesEpaymentChain.execute(paymentPeriodRq, rulesPaymentPeriod);
        }
        else
        {
            rulesPaymentPeriod = m_rulesPrintPaymentChain.execute(paymentPeriodRq, rulesPaymentPeriod);
        }
        return rulesPaymentPeriod;
	}

	/// <summary>
	/// Returns a payment period based on user-defined frequencies and policies.
	/// </summary>
	/// <param name="paymentPeriodRq">A Request instance with the proper variable set, otherwise SpcfIllegalArgumentException will be thrown.</param>
	/// <returns>An instance of a IPaymentPeriod with the correct Due date set.</returns>
	/// <exception cref="SpcfIllegalArgumentException">Thrown when missing arguments exist.</exception>
	protected IPaymentPeriod getUserDefinedPaymentPeriod( IPaymentPeriodRequest paymentPeriodRq )
	{
		// Extra step to convert Enums to Strings used in the rules.
		setFrequencyId(paymentPeriodRq);
        PaymentPeriod customPaymentPeriod = null;
        if (paymentPeriodRq.getSubmitMethodId().equals("EFE-EPAY"))
        {
            customPaymentPeriod = m_userEpaymentChain.execute(paymentPeriodRq, customPaymentPeriod );
        }
        else
        {
            customPaymentPeriod = m_userPrintPaymentChain.execute(paymentPeriodRq, customPaymentPeriod );
        }
        return customPaymentPeriod;
	}

    /// <summary>
    /// Gets the due date for a payment using the supplied payment period request as an input to the calculator.
    /// </summary>
    /// <param name="paymentPeriodRq">Payment period request to calculate the due date for</param>
    /// <returns>RulesCalendar instance set to the due date the payment needs to be received by the agency.</returns>
    public RulesCalendar getPaymentDueDate (IPaymentPeriodRequest paymentPeriodRq) {
        PaymentPeriod rulesPaymentPeriod = null;
        rulesPaymentPeriod = m_rulesDueDateChain.execute(paymentPeriodRq, rulesPaymentPeriod);
        return rulesPaymentPeriod.getDueDate(); 
    }

    /// <summary>
	/// This Strategy only includes filters required for Print due dates.
	/// </summary>
	/// <returns>A Filter that can be executed.</returns>
	protected static DueDateFilter applyRulesPrintStrategy()
	{
		/* 7th filter */	DueDateFormatFilter formatFilter = new DueDateFormatFilter();
		/* 6th filter */	DueDateHolidayFilter holidayFilter = new DueDateHolidayFilter(formatFilter);
		/* 5th filter */	DueDateWeekendFilter weekendFilter = new DueDateWeekendFilter(holidayFilter);
        /* 4th filter */	DueDatePeriodBoundaryFilter boundaryFilter = new DueDatePeriodBoundaryFilter(weekendFilter);
		/* 3rd filter */	DueDateHolidayAllowanceFilter allowanceFilter = new DueDateHolidayAllowanceFilter(boundaryFilter);
		/* 2nd filter */	DueDatePaymentPeriodFilter periodFilter = new DueDatePaymentPeriodFilter(allowanceFilter);
		/* 1st filter */	return new DueDateRulesValidationFilter(periodFilter);
	}

	/// <summary>
	/// This Strategy includes filters to handle agency and EFE submission deadlines.
	/// </summary>
	/// <returns>A Filter that can be executed.</returns>
	protected static DueDateFilter applyRulesEpayStrategy() 
	{
		/* 9th filter */	DueDateFormatFilter formatFilter = new DueDateFormatFilter();
		/* 8th filter */	DueDateEfeFilingDeadlineFilter efeFilter = new DueDateEfeFilingDeadlineFilter(formatFilter);
		/* 7th filter */	DueDateAgencySendByOffsetFilter sendByFilter = new DueDateAgencySendByOffsetFilter(efeFilter);
		/* 6th filter */	DueDateHolidayFilter holidayFilter = new DueDateHolidayFilter(sendByFilter);
		/* 5th filter */	DueDateWeekendFilter weekendFilter = new DueDateWeekendFilter(holidayFilter);
        /* 4th filter */	DueDatePeriodBoundaryFilter boundaryFilter = new DueDatePeriodBoundaryFilter(weekendFilter);
		/* 3rd filter */	DueDateHolidayAllowanceFilter allowanceFilter = new DueDateHolidayAllowanceFilter(boundaryFilter);
		/* 2nd filter */	DueDatePaymentPeriodFilter periodFilter = new DueDatePaymentPeriodFilter(allowanceFilter);
		/* 1st filter */	return new DueDateRulesValidationFilter(periodFilter);
	}

    /// <summary>
	/// This strategy creates a calculator that will calculate the due date for a payment, regardless of submit method
	/// </summary>
	/// <returns>A Filter that can be executed.</returns>
    /// <remarks>Note that this filter will calculate the due date for either a print or e-pay payment.
    ///  It simply returns when the agency expects to have the payment by.</remarks>
    protected static DueDateFilter applyRulesDueDateStrategy()
	{
		/* 7th filter */	DueDateFormatFilter formatFilter = new DueDateFormatFilter();
        /* 6th filter */	DueDateHolidayFilter holidayFilter = new DueDateHolidayFilter(formatFilter);
        /* 5th filter */	DueDateWeekendFilter weekendFilter = new DueDateWeekendFilter(holidayFilter);
        /* 4TH filter */	DueDatePeriodBoundaryFilter boundaryFilter = new DueDatePeriodBoundaryFilter(weekendFilter);
        /* 3RD filter */	DueDateHolidayAllowanceFilter allowanceFilter = new DueDateHolidayAllowanceFilter(boundaryFilter);
        /* 2nd filter */	DueDatePaymentPeriodFilter periodFilter = new DueDatePaymentPeriodFilter(allowanceFilter);
        /* 1st filter */	return new DueDateRulesValidationFilter(periodFilter);
	}

    /// <summary>
	/// This Strategy only includes filters required for custom or user defined due dates (Print).
	/// </summary>
	/// <returns>A Filter that can be executed.</returns>
	protected static DueDateFilter applyCustomPrintStrategy()
	{
		/* 4th filter */	DueDateFormatFilter formatFilter = new DueDateFormatFilter();
		/* 3rd filter */	DueDateCustomScheduleFilter customFilter = new DueDateCustomScheduleFilter(formatFilter);
		/* 2nd filter */	DueDatePaymentPeriodFilter periodFilter = new DueDatePaymentPeriodFilter(customFilter);
		/* 1st filter */	return new DueDateUserValidationFilter(periodFilter);
	}

	/// <summary>
	/// This Strategy includes filters to handle user defined due dates and EFE submission deadlines .
	/// </summary>
	/// <returns>A Filter that can be executed.</returns>
	protected static DueDateFilter applyCustomEpayStrategy()
	{
		/* 5th filter */	DueDateFormatFilter formatFilter = new DueDateFormatFilter();
		/* 4th filter */	DueDateEfeFilingDeadlineFilter efeFilter = new DueDateEfeFilingDeadlineFilter(formatFilter);
		/* 3rd filter */	DueDateCustomScheduleFilter customFilter = new DueDateCustomScheduleFilter(efeFilter);
		/* 2nd filter */	DueDatePaymentPeriodFilter periodFilter = new DueDatePaymentPeriodFilter(customFilter);
		/* 1st filter */	return new DueDateUserValidationFilter(periodFilter);
	}

	/// <summary>
	/// Utility methat that sets the Frequency Id to a Rules one from a User one.
	/// </summary>
	/// <param name="paymentPeriodRq"></param>
	protected void setFrequencyId(IPaymentPeriodRequest paymentPeriodRq)
	{
		// This maps the user frequency ENUM values to values we can use in the rules.xml
        UserFrequencyType freq = paymentPeriodRq.getUserFrequency();
        if (freq == UserFrequencyType.FedAnnual || freq == UserFrequencyType.CustomAnnual)
        {
            paymentPeriodRq.setFrequencyId(ANNUAL_FREQUENCY);
        }
        else if (freq == UserFrequencyType.FedMonthly || freq == UserFrequencyType.CustomMonthly) 
        {
            paymentPeriodRq.setFrequencyId(MONTHLY_FREQUENCY);
        }
        else if (freq == UserFrequencyType.FedNextDay || freq == UserFrequencyType.AfterEveryPayrollRun) 
		{
		 	paymentPeriodRq.setFrequencyId(NEXTBANKINGDAY_FREQUENCY);
		}
        else if (freq == UserFrequencyType.FedQuarterly || freq == UserFrequencyType.CustomQuarterly) 
		{
		 	paymentPeriodRq.setFrequencyId(QUARTERLY_FREQUENCY);
		}
        else if (freq == UserFrequencyType.FedSemiweekly) 
		{
		 	paymentPeriodRq.setFrequencyId(SEMIWEEKLY_FREQUENCY);
		}
        else if (freq == UserFrequencyType.CustomWeekly) 
		{
		 	paymentPeriodRq.setFrequencyId(WEEKLY_FREQUENCY);
		}
		else
		{
			throw new RuntimeException("PaymentPeriodRequest argument is invalid: " + "UserFrequencyType=" + paymentPeriodRq.getUserFrequency().toString());
		}
	}
}