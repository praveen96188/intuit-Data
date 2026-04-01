package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class PaymentRequirement extends BasePaymentRequirement {

	/**
	 * Default constructor.
	 */
	public PaymentRequirement()
	{
		super();
	}

    @Override
    public final boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        //always met on the global level
        return true;
    }

    //"abstract"
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod, MoneyMovementTransaction payment) {
        return false;
    }

}