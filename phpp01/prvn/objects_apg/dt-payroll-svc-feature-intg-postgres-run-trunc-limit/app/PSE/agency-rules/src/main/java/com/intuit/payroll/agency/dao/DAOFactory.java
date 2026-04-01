//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;
/// <summary>
/// Abstract class from which DAO related factories should derive.  Decouples client
/// code from knowledge of underlying data persistence mechanism.
/// </summary>
/// <remarks>
/// Defines abstract methods that return instsances of DAO objects.
/// 
/// Pattern: Factory
/// </remarks>
public abstract class DAOFactory
{
//  don't add static members (and locks) until performance data shows it is worthwhile
//	private static DAOFactory instance;

	public static DAOFactory getDAOFactory() 
	{
		return DataStore.getDataStore();
	}

	public abstract IAgencyDAO getAgencyDAO();
	public abstract IJurisdictionDAO getJurisdictionDAO();
	public abstract IPaymentFrequencyDAO getPaymentFrequencyDAO();
	public abstract IPaymentTemplateDAO getPaymentTemplateDAO();
	public abstract IPaymentPeriodDAO getPaymentPeriodDAO();
	public abstract ISubmitMethodDAO getSubmitMethodDAO();
	public abstract IPaymentReasonDAO getPaymentReasonDAO();
	public abstract IEnrollmentGroupDAO getEnrollmentGroupDAO();
	public abstract ICredentialDataDAO getCredentialDataDAO();
	public abstract IMetaDataDAO getMetaDataDAO();
	public abstract IEfeRulesDAO getEfeRulesDAO();
	public abstract IHolidayGroupDAO getHolidayGroupDAO();
	public abstract IFormTemplateDAO getFormTemplateDAO();
	public abstract IFormFilingFrequencyDAO getFormFilingFrequencyDAO();
	public abstract ILawTypeDAO getLawTypeDAO();
	public abstract IFormInfoDAO getFormInfoDAO();
	public abstract IFormSubmitMethodDAO getFormSubmitMethodDAO();
	public abstract IFormTemplateGroupDAO getFormTemplateGroupDAO();
}
