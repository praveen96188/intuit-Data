package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;

/**
 * Hand-written business logic
 */
public class CompanyTFSSubmission extends BaseCompanyTFSSubmission {

	/**
	 * Default constructor.
	 */
	public CompanyTFSSubmission()
	{
		super();
	}

    public static CompanyTFSSubmission findCompanyTFSSubmission(Company pCompany, int pYear) {
        Criterion<CompanyTFSSubmission> where = CompanyTFSSubmission.Company().equalTo(pCompany)
                                                                              .And(CompanyTFSSubmission.Year().equalTo(pYear));


        DomainEntitySet<CompanyTFSSubmission> companyTFSSubmissions = Application.find(CompanyTFSSubmission.class, where);
        if (companyTFSSubmissions != null && companyTFSSubmissions.size() > 0) {
            return companyTFSSubmissions.get(0) ;
        }
        return null;
    }

}