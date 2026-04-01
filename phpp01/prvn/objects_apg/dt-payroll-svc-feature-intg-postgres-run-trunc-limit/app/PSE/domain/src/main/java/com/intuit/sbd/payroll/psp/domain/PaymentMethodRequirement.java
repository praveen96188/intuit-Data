package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class PaymentMethodRequirement extends BasePaymentMethodRequirement {

	/**
	 * Default constructor.
	 */
	public PaymentMethodRequirement()
	{
		super();
	}

    //"abstract"
    @SuppressWarnings({"UnusedParameters"})
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        return false;
    }

    //"abstract"
    public String getRequirementString(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        return null;
    }

}