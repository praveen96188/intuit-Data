package com.intuit.sbd.payroll.psp.domain;

import org.apache.commons.lang.StringUtils;

/**
 * Hand-written business logic
 */
public class SystemPaymentRequirement extends BaseSystemPaymentRequirement {

	/**
	 * Default constructor.
	 */
	public SystemPaymentRequirement()
	{
		super();
	}

    @Override
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod, MoneyMovementTransaction payment) {
        switch (getSystemRequirementType()) {
            case LAAIDDF:
                //Must have AID if DF of SM
                return StringUtils.isNotEmpty(companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getAgencyTaxpayerId())
                        || payment.getPaymentFrequency().getPaymentFrequencyId() != DepositFrequencyCode.SEMIMONTHLY;
            default:
                throw new RuntimeException(getSystemRequirementType().name() + " not an implemented type");
        }
    }

    @Override
    public String getRequirementString(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        switch (getSystemRequirementType()) {
            case LAAIDDF:
                return "Agency ID required if SEMIMONTHLY";
            default:
                throw new RuntimeException(getSystemRequirementType().name() + " not an implemented type");
        }
    }
}