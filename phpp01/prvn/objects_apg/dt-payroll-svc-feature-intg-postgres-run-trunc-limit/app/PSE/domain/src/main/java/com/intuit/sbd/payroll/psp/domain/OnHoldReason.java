package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Set;

/**
 * Hand-written business logic
 */
public class OnHoldReason extends BaseOnHoldReason {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public OnHoldReason()
	{
		super();
	}

	public static Set<Company> getHoldStatusChangedCompanies(SpcfCalendar lastProcessedTime, SpcfCalendar now) {

		Criterion<DomainEntity> onHoldReasonCriteria = OnHoldReason.ModifiedDate().greaterThan(lastProcessedTime)
				.And(OnHoldReason.ModifiedDate().lessOrEqualThan(now));

		Expression<OnHoldReason> OnHoldReasonQuery = new Query<OnHoldReason>().Where(onHoldReasonCriteria)
				.OrderBy(OnHoldReason.ModifiedDate());

		DomainEntitySet<OnHoldReason> onHoldReasonSet = Application.find(OnHoldReason.class, OnHoldReasonQuery);

		Set<Company> companySet = new HashSet<>();
		for(OnHoldReason onHoldReason : onHoldReasonSet) {
			Company company = onHoldReason.getCompany();
			if (company instanceof HibernateProxy) {
				company = (Company) ((HibernateProxy) company).getHibernateLazyInitializer().getImplementation();
			}
			companySet.add(company);
		}

		return companySet;
	}

}