package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class PaycheckUsageHist extends BasePaycheckUsageHist {

	/**
	 * Default constructor.
	 */
	public PaycheckUsageHist()
	{
		super();
	}

	public PaycheckUsageHist(PaycheckUsage paycheckUsage, PaycheckUsageHist oldPaycheckUsageHist)
	{
		setPaycheckUsage(paycheckUsage);
		setCompany(paycheckUsage.getCompany());
		setEmployeeUsage(oldPaycheckUsageHist.getEmployeeUsage());
		setNotes(oldPaycheckUsageHist.getNotes());
	}

}