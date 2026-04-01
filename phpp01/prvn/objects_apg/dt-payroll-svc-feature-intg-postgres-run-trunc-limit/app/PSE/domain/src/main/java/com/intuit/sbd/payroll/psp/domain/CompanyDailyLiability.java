package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class CompanyDailyLiability extends BaseCompanyDailyLiability {

	/**
	 * Default constructor.
	 */
	public CompanyDailyLiability()
	{
		super();
	}

    public static DomainEntitySet<CompanyDailyLiability> findCompanyDailyLiabilitiesBetweenDates(SpcfCalendar startDate, SpcfCalendar endDate, SpcfCalendar lastRunDate) {
        Criterion<CompanyDailyLiability> liabilityWhereClause = CompanyDailyLiability.LiabilityDate().lessOrEqualThan(endDate);

        if (startDate!=null) {
               liabilityWhereClause = liabilityWhereClause.And(CompanyDailyLiability.LiabilityDate().greaterOrEqualThan(startDate));
        }

        if (lastRunDate!=null) {
            liabilityWhereClause = liabilityWhereClause.And(CompanyDailyLiability.ModifiedDate().greaterOrEqualThan(lastRunDate));
        }

        Expression<CompanyDailyLiability> query =
                new Query<CompanyDailyLiability>()
                        .Where(liabilityWhereClause).OrderBy(CompanyDailyLiability.Company(), CompanyDailyLiability.LiabilityDate(), CompanyDailyLiability.Law());

        DomainEntitySet<CompanyDailyLiability> companyDailyLiabilities = Application.find(CompanyDailyLiability.class, query);

        return companyDailyLiabilities;
    }

    public static DomainEntitySet<CompanyDailyLiability> findCompanyDailyLiabilitiesBetweenDates(Company company, SpcfCalendar startDate, SpcfCalendar endDate, Law law) {
        Criterion<CompanyDailyLiability> liabilityWhereClause = CompanyDailyLiability.Company().equalTo(company)
                .And(CompanyDailyLiability.Law().equalTo(law));

        if (startDate!=null && endDate!=null) {
            liabilityWhereClause = liabilityWhereClause.And(CompanyDailyLiability.LiabilityDate().between(startDate,endDate));
        }


        Expression<CompanyDailyLiability> query =
                new Query<CompanyDailyLiability>()
                        .Where(liabilityWhereClause).OrderBy(CompanyDailyLiability.Company(), CompanyDailyLiability.LiabilityDate(), CompanyDailyLiability.Law());

        DomainEntitySet<CompanyDailyLiability> companyDailyLiabilities = Application.find(CompanyDailyLiability.class, query);

        return companyDailyLiabilities;
    }

}