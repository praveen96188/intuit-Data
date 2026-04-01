package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class ATFPayrollsToProcess extends BaseATFPayrollsToProcess {

	/**
	 * Default constructor.
	 */
	public ATFPayrollsToProcess()
	{
		super();
	}

    public static boolean payrollRunExistsForProcessing(PayrollRun pPayrollRun) {
        Expression<ATFPayrollsToProcess> query = new Query<ATFPayrollsToProcess>()
                .Where(ATFPayrollsToProcess.PayrollRun().equalTo(pPayrollRun));
        return Application.find(ATFPayrollsToProcess.class, query).size()>0;
    }
}