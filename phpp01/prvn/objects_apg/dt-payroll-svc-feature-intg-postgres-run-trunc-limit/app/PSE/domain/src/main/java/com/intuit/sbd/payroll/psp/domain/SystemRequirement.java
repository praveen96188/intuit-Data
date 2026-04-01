package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class SystemRequirement extends BaseSystemRequirement {

	/**
	 * Default constructor.
	 */
	public SystemRequirement()
	{
		super();
	}

    @Override
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        switch (getSystemRequirementType()) {
            case EFTPSEnrollment:
                return companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getCompanyAgency().getCompany().getCurrentEnrollmentStatus() == EftpsEnrollmentStatus.Enrolled;
            case ACHEnrollment:
                ACHEnrollment achEnrollment = companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getCompanyAgency().getCompany().getAllACHEnrollments()
                                                   .find(ACHEnrollment.Status().in(ACHEnrollmentStatus.Enrolled, ACHEnrollmentStatus.Deleted))
                                                   .sort(ACHEnrollment.StatusEffectiveDate().Descending()).getFirst();
                if(achEnrollment != null && achEnrollment.getStatus().equals(ACHEnrollmentStatus.Enrolled)){
                    return true;
                } else {
                    return false;
                }
            default:
                throw new RuntimeException(getSystemRequirementType().name() + " not an implemented type");
        }
    }

    @Override
    public String getRequirementString(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        String message = "";
        switch (getSystemRequirementType()){
            case LAAIDDF:
                message = "LAAIDDF";
                break;
            case ACHEnrollment:
                message = "ACH Enrollment not completed";
                break;
            case EFTPSEnrollment:
                message = "EFTPS Enrollment not completed";
        }
        return message;
    }
}