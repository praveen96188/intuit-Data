package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class DepositFrequencyRequirement extends BaseDepositFrequencyRequirement {

	/**
	 * Default constructor.
	 */
	public DepositFrequencyRequirement()
	{
		super();
	}

    @Override
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod, MoneyMovementTransaction payment) {
        return payment.getPaymentFrequency().getPaymentFrequencyId() != getProhibitedDepositFrequency();
    }

    @Override
    public String getRequirementString(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        return "Deposit Frequency must not be " + getProhibitedDepositFrequency().toString();
    }
}