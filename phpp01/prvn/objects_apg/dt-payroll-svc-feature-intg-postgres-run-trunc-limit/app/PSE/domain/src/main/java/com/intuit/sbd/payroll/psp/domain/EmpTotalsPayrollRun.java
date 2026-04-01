package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class EmpTotalsPayrollRun extends BaseEmpTotalsPayrollRun {

    /**
     * Default constructor.
     */
    public EmpTotalsPayrollRun() {
        super();
    }
    
    public EmpTotalsPayrollRun updateEmpTotalsPayrollRunStatus(EmpTotalsPayrollStatus pEmpTotalsPayrollStatus) {
		setStatus(pEmpTotalsPayrollStatus);
		return Application.save(this);
	}

    public static void insertEmpTotalsPayrollRun(PayrollRun pPayrollRun) {
        if (pPayrollRun != null && pPayrollRun.getCompany().isCompanyOnService(ServiceCode.Tax)) {
            pPayrollRun.updateEECalculationToken(); // Todo, Remove this when column is dropped
            EmpTotalsPayrollRun empTotalsPayrollRun = new EmpTotalsPayrollRun();
            empTotalsPayrollRun.setPayrollRun(pPayrollRun);
            empTotalsPayrollRun.setCompany(pPayrollRun.getCompany());
            empTotalsPayrollRun.setQuarterStartDate(CalendarUtils.getFirstDayOfQuarter(pPayrollRun.getPaycheckDate()));
            empTotalsPayrollRun.setStatus(EmpTotalsPayrollStatus.Pending);
            Application.save(empTotalsPayrollRun);
        }
    }
    
    public static EmpTotalsPayrollRun findLatestEmpTotalsPayrollRun(final Company pCompany,
			final SpcfCalendar pQuarterStartDate, final EmpTotalsPayrollStatus pEmpTotalsPayrollStatus) {

		Criterion<EmpTotalsPayrollRun> where = EmpTotalsPayrollRun.Company().equalTo(pCompany)
				.And(EmpTotalsPayrollRun.QuarterStartDate().equalTo(pQuarterStartDate))
				.And(EmpTotalsPayrollRun.Status().equalTo(pEmpTotalsPayrollStatus));
		
		Expression<EmpTotalsPayrollRun> query =
                new Query<EmpTotalsPayrollRun>()
                        .Where(where)
                        .OrderBy(EmpTotalsPayrollRun.CreatedDate().Descending()).LimitResults(0, 1);

		DomainEntitySet<EmpTotalsPayrollRun> empTotalsPayrollRuns = Application.find(EmpTotalsPayrollRun.class, query);

		if (empTotalsPayrollRuns.size() > 0) {
			return empTotalsPayrollRuns.get(0);
		}
		return null;
	}
}
