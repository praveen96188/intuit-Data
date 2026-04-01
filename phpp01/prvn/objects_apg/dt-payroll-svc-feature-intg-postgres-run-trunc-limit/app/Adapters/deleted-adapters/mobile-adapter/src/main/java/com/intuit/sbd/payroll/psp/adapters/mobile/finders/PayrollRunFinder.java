package com.intuit.sbd.payroll.psp.adapters.mobile.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollType;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jan 11, 2011
 * Time: 10:43:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class PayrollRunFinder {

    public static DomainEntitySet<PayrollRun> findPayrollRuns(Company pCompany, int pDays) {

        SpcfCalendar xDaysAgo = PSPDate.getPSPTime();
        xDaysAgo.addDays(pDays * -1);

        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                       .Where(PayrollRun.Company().equalTo(pCompany)
                              .And(PayrollRun.PayrollRunType().in(PayrollType.Regular, PayrollType.BillPayment)
                              .And(PayrollRun.PayrollRunDate().greaterOrEqualThan(xDaysAgo))))
                       .OrderBy(PayrollRun.PayrollRunDate().Descending());
        DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, query);

        if (payrollRuns.isEmpty()) {
            query =
                    new Query<PayrollRun>()
                           .Where(PayrollRun.Company().equalTo(pCompany)
                                  .And(PayrollRun.PayrollRunType().in(PayrollType.Regular, PayrollType.BillPayment)))
                           .OrderBy(PayrollRun.PayrollRunDate().Descending())
                           .LimitResults(0,1);
            payrollRuns = Application.find(PayrollRun.class, query);
        }

        return payrollRuns;
    }

    public static DomainEntitySet<PayrollRun> findPayrollRuns(Company pCompany, int pStart, int pSize) {
        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                       .Where(PayrollRun.Company().equalTo(pCompany))
                              //.And(PayrollRun.PayrollRunType().in(PayrollType.Regular, PayrollType.BillPayment)))
                       .OrderBy(PayrollRun.PayrollRunDate().Descending())
                       .LimitResults(pStart, pSize);
        return Application.find(PayrollRun.class, query);
    }
}
