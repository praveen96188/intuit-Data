package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class CompanyFilingAmount extends BaseCompanyFilingAmount {

	/**
	 * Default constructor.
	 */
	public CompanyFilingAmount()
	{
		super();
	}

    public CompanyFilingAmount(CompanyAgencyPaymentTemplate pCompanyAgencyPaymentTemplate, String pName,
                               SpcfCalendar pEffectiveDate, double pAmount)
    {
        setCompanyAgencyPaymentTemplate(pCompanyAgencyPaymentTemplate);
        setName(pName);
        setEffectiveDate(pEffectiveDate);
        setAmount(pAmount);
    }

    public AdditionalFilingAmount getAdditionalFilingAmount() {
        return getCompanyAgencyPaymentTemplate().getPaymentTemplate().getAdditionalFilingAmountCollection()
                .findEntity(AdditionalFilingAmount.Name().equalTo(getName()));
    }

    public static DomainEntitySet<CompanyFilingAmount> findCompanyFilingAmounts(CompanyAgencyPaymentTemplate capt,
                                                                         String name,
                                                                         SpcfCalendar effectiveDate) {
        return Application.find(CompanyFilingAmount.class,
                         CompanyFilingAmount.CompanyAgencyPaymentTemplate().equalTo(capt)
                                            .And(CompanyFilingAmount.Name().equalTo(name))
                                            .And(CompanyFilingAmount.EffectiveDate().equalTo(effectiveDate))
                                            .And(CompanyFilingAmount.InvalidDate().isNull()));

    }

}