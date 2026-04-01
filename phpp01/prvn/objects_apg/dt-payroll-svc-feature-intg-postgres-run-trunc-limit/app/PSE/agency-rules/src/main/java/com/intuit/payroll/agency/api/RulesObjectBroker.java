//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

import com.intuit.payroll.agency.impl.*;

/// <summary>
/// RulesObjectBroker is a lightweight object broker that allows clients to program against this
/// component without knowledge of the implementation of the services it provides.
/// 
/// Pattern: Factory
/// 
/// TODO: This could be enhanced significantly to allow for objects to be found my moniker and/or
/// their own interface.
/// </summary>
public final class RulesObjectBroker
{
//	private static final RulesObjectBroker instance = new RulesObjectBroker();

	private IRulesInfo rulesInfoInstance;
	private IFormRulesInfo formRulesInfoInstance;
	
	private RulesObjectBroker()
	{
	}

	/// <summary>
	/// Use this static method to get an instance.  Not a singleton (yet), could be but would have to lock.
	/// </summary>
	/// <returns>An instance of the Broker.</returns>
	public static RulesObjectBroker getInstance()
	{
		return new RulesObjectBroker();
	}

	/// <summary>
	/// Returns an instance of IRulesInfo implementation.
	/// </summary>
	/// <returns>An instance of IRulesInfo</returns>
	public IRulesInfo getRulesInfo()
	{
		if (rulesInfoInstance == null) 
		{
			rulesInfoInstance = new RulesInfo();
		}
		return rulesInfoInstance;
	}
	
	/// <summary>
	/// Returns an instance of IFormRulesInfo implementation.
	/// </summary>
	/// <returns>An instance of IFormRulesInfo.</returns>
	public IFormRulesInfo getFormRulesInfo()
	{
		if (formRulesInfoInstance == null) 
		{
			formRulesInfoInstance = new FormRulesInfo();
		}
		return formRulesInfoInstance;
	}

	/// <summary>
	/// Payment period object builder.
	/// </summary>
	/// <returns>A new IPaymentPeriod object.</returns>
	public IPaymentPeriod createPaymentPeriod()
	{
		return new PaymentPeriod();
	}

	/// <summary>
	/// Payment period request object builder
	/// </summary>
	/// <returns>A new IPaymentPeriodRequest object</returns>
	public IPaymentPeriodRequest createPaymentPeriodRequest()
	{
		return new PaymentPeriodRequest();
	}

	/// <summary>
	/// Payment Template object builder.
	/// </summary>
	/// <returns>A new IRulesPaymentTemplate instance.</returns>
	public IRulesPaymentTemplate createPaymentTemplate()
	{
		return PaymentTemplate.createPaymentTemplate();
	}
    
	/// <summary>
	/// creates an IRules list from a given ICollection
	/// </summary>
	/// <param name="collection">ICollection object that it will create an IRulesList for.</param>
	/// <returns>IRulesList object with the contents of the given collection.</returns>
	public IRulesList createRulesList(Iterable collection)
	{
		IRulesList rulesList = new RulesList();
		rulesList.clear();

		if (collection != null) 
		{
			for(Object element : collection)
			{
				rulesList.add(element);
			}
		}

		return rulesList;
	}
	
}
