package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class CompanyAgencyFormTemplate extends BaseCompanyAgencyFormTemplate {

	/**
	 * Default constructor.
	 */
	public CompanyAgencyFormTemplate()
	{
		super();
	}

    public static DomainEntitySet<CompanyAgencyFormTemplate> getCompanyAgencyFTCollection() {
        return Application.find(CompanyAgencyFormTemplate.class);

    }
}