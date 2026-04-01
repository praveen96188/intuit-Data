package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class AdditionalFilingAmount extends BaseAdditionalFilingAmount {

	/**
	 * Default constructor.
	 */
	public AdditionalFilingAmount()
	{
		super();
	}

    public static AdditionalFilingAmount findByName(String name) {
        DomainEntitySet<AdditionalFilingAmount> additionalFilingAmounts = Application.find(AdditionalFilingAmount.class, AdditionalFilingAmount.Name().equalTo(name));
        if (additionalFilingAmounts.size() > 1) {
            throw new RuntimeException("Multiple additional filing amounts for " + name); //indicates a _static data_ problem
        }

        return additionalFilingAmounts.getFirst();
    }


}