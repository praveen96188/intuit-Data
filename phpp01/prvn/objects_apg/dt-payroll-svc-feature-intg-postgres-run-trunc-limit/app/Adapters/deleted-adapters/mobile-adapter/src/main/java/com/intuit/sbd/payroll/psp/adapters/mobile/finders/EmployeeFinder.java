package com.intuit.sbd.payroll.psp.adapters.mobile.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * @author Jeff Jones
 */
public class EmployeeFinder {

    public static Employee findEmployee(Company pCompany, String pId) {
        return Application.findById(Employee.class, SpcfUniqueId.createInstance(pId));
    }

    public static DomainEntitySet<Employee> findEmployees(Company pCompany) {
        Expression<Employee> query =
                new Query<Employee>()
                       .Where(Employee.Company().equalTo(pCompany))
                       .OrderBy(Employee.LastName());

        return Application.find(Employee.class, query);
    }

}
