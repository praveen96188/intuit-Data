package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class CompanyPaymentTemplatePaymentMethod extends BaseCompanyPaymentTemplatePaymentMethod {

	/**
	 * Default constructor.
	 */
	public CompanyPaymentTemplatePaymentMethod()
	{
		super();
	}

    /**
     * Update <property>enabled</property> based on if requirements are met
     * @return true if <property>enabled</property> has changed
     */
    public boolean recalculatePaymentEnabled() {
        boolean oldValue = getEnabled();
        boolean allRequirementsMet = true;
        PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplatePaymentMethod(getPaymentMethod());
        if (paymentTemplatePaymentMethod == null) {
            //scenario: payment method becomes invalid for all companies, temporary or permanently
            allRequirementsMet = false;
        } else {
            for (PaymentMethodRequirement requirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
                allRequirementsMet = requirement.isRequirementMet(this);
                if (!allRequirementsMet) {
                    break;
                }
            }
        }
        setEnabled(allRequirementsMet);
        return oldValue != getEnabled();
    }


    //like getEnabled, but as applies to a specific payment
    public boolean getEnabledForPayment(MoneyMovementTransaction payment) {
        if (payment == null) {
            return getEnabled();
        }
        if (! getEnabled()) {
            return false;
        }
        PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplatePaymentMethod(getPaymentMethod());
        boolean allRequirementsMet = true;
        for (PaymentMethodRequirement requirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
            if (requirement instanceof PaymentRequirement) {
                allRequirementsMet = ((PaymentRequirement) requirement).isRequirementMet(this, payment);
                if (!allRequirementsMet) {
                    break;
                }
            }
        }
        return allRequirementsMet;
    }

    public static CompanyPaymentTemplatePaymentMethod createNewCompanyPaymentTemplatePaymentMethod(PaymentTemplatePaymentMethod paymentMethod, CompanyAgencyPaymentTemplate capt) {
        CompanyPaymentTemplatePaymentMethod companyPaymentMethod = new CompanyPaymentTemplatePaymentMethod();

        companyPaymentMethod.setAgentEnabled(paymentMethod.getPaymentMethod() != PaymentMethod.ACHCredit); //Behavior for ACH is approval--otherwise just something that can be turned off if needed
        companyPaymentMethod.setPaymentMethod(paymentMethod.getPaymentMethod());
        companyPaymentMethod.setCompanyAgencyPaymentTemplate(capt);
        companyPaymentMethod.recalculatePaymentEnabled();

        capt.addCompanyPaymentTemplatePaymentMethod(companyPaymentMethod);
        Application.save(companyPaymentMethod);

        return companyPaymentMethod;
    }

}
