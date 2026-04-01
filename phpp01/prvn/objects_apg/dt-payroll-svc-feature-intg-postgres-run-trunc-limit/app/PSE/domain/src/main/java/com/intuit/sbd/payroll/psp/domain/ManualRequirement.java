package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class ManualRequirement extends BaseManualRequirement {

	/**
	 * Default constructor.
	 */
	public ManualRequirement()
	{
		super();
	}

    @Override
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        return companyPaymentTemplatePaymentMethod.getAgentEnabled();
    }

    @Override
    public String getRequirementString(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        return "ACH Registered must be set";
    }
}