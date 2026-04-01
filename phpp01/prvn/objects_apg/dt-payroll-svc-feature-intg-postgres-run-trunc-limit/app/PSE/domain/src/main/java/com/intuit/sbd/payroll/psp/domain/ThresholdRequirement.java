package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class ThresholdRequirement extends BaseThresholdRequirement {

	/**
	 * Default constructor.
	 */
	public ThresholdRequirement()
	{
		super();
	}

    @Override
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod, MoneyMovementTransaction payment) {
        return payment.getMoneyMovementTransactionAmount().isLessThanEqualTo(getMaximumPaymentAmount());
    }

    @Override
    public String getRequirementString(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        return "Payment amount may not exceed $" + getMaximumPaymentAmount();
    }
}