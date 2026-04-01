package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class UsagePeriod extends BaseUsagePeriod {

	/**
	 * Default constructor.
	 */
	public UsagePeriod() {
		super();
	}

	public static UsagePeriod findUsagePeriod(CompanyUsage pCompanyUsage, SpcfCalendar pStartDate, SpcfCalendar pEndDate) {
		UsagePeriod foundUsagePeriod = null;

		NaturalKey naturalKey = new NaturalKey(UsagePeriod.class, pCompanyUsage.getId(), pStartDate, pEndDate);
		SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

		if (primaryKey != null) {
			foundUsagePeriod = Application.findById(UsagePeriod.class, primaryKey);
		} else {
			DomainEntitySet<UsagePeriod> usagePeriods = Application.find(UsagePeriod.class, UsagePeriod.CompanyUsage().equalTo(pCompanyUsage).And(UsagePeriod.StartDate().equalTo(pStartDate)).And(UsagePeriod.EndDate().equalTo(pEndDate)));

			if (usagePeriods.size() > 1) {
				throw new RuntimeException("Query for usage periods by company" + pCompanyUsage + " and start date" + pStartDate + " and end date " + pEndDate + " did not return 0 or 1 results as expected");
			}

			if (!usagePeriods.isEmpty()) {
				foundUsagePeriod = usagePeriods.get(0);
				Application.getSessionCache().addPrimaryKey(naturalKey, foundUsagePeriod.getId());
			}
		}

		return foundUsagePeriod;
	}

	public static UsagePeriod createUsagePeriod(CompanyUsage pCompanyUsage, SpcfCalendar pStartDate, SpcfCalendar pEndDate) {
		UsagePeriod createdUsagePeriod = new UsagePeriod();

		createdUsagePeriod.setCompanyUsage(pCompanyUsage);
		createdUsagePeriod.setStartDate(pStartDate);
		createdUsagePeriod.setEndDate(pEndDate);

		Application.save(createdUsagePeriod);

		NaturalKey naturalKey = new NaturalKey(UsagePeriod.class, pCompanyUsage.getId(), pStartDate, pEndDate);
		Application.getSessionCache().addPrimaryKey(naturalKey, createdUsagePeriod.getId());

		return createdUsagePeriod;
	}

	public static UsagePeriod findOrCreateUsagePeriod(CompanyUsage pCompanyUsage, SpcfCalendar pStartDate, SpcfCalendar pEndDate) {
		UsagePeriod aUsagePeriod = findUsagePeriod(pCompanyUsage, pStartDate, pEndDate);
		if (aUsagePeriod == null) {
			aUsagePeriod = createUsagePeriod(pCompanyUsage, pStartDate, pEndDate);
		}
		return aUsagePeriod;
	}
}
