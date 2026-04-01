package com.intuit.sbd.payroll.psp.adapters.mobile.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * @author Jeff Jones
 */
public class MobileFinder {

    public static DomainEntitySet<Paycheck> findPaychecksByEmployee(Company pCompany, Employee pEmployee, int pStart, int pSize) {
        Expression<Paycheck> query = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                        .And((Paycheck.DDEmployee().equalTo(pEmployee).Or(Paycheck.SourceEmployee().equalTo(pEmployee)))))
                .OrderBy(Paycheck.PayrollRun().PaycheckDate())
                .LimitResults(pStart, pSize);

        return Application.find(Paycheck.class, query);
    }

}
