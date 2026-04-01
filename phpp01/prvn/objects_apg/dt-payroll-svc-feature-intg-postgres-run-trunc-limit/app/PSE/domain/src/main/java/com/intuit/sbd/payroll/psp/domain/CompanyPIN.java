package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.util.PINUtils;

/**
 * Hand-written business logic
 */
public class CompanyPIN extends BaseCompanyPIN {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public CompanyPIN()
	{
		super();
	}

    public static CompanyPIN createCompanyPIN(Company company, String pin) {
        CompanyPIN companyPin = new CompanyPIN();
        companyPin.setHashType(PINUtils.CURRENT_HASH_TYPE);
        companyPin.setPINValue(PINUtils.encrypt(pin));
        companyPin.setCompany(company);
        return companyPin;
    }
}