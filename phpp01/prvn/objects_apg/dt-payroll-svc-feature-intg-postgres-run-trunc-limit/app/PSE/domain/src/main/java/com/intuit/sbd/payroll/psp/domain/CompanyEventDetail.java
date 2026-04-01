package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class CompanyEventDetail extends BaseCompanyEventDetail {

	/**
	 * Default constructor.
	 */
	public CompanyEventDetail()
	{
		super();
	}

    public static DomainEntitySet<CompanyEventDetail> findCompanyEventDetails(Company pCompany) {
        DomainEntitySet<CompanyEventDetail> companyEventDetails = Application.find(CompanyEventDetail.class,
                CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany));

        return companyEventDetails;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder .append("Event Detail")
                .append("  TypeCd: ").append(getEventDetailTypeCd().name());
        if (getEventDetailSubtype() != null) {
            builder.append("  SubType: ").append(getEventDetailTypeCd());
        }
        builder.append("  Value: ").append(getValue());
        return builder.toString();
    }

}