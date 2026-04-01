package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class VmpEmployeeInfo extends BaseVmpEmployeeInfo {

	/**
	 * Default constructor.
	 */
	public VmpEmployeeInfo()
	{
		super();
	}

    /**
     * This method returns the employee records which are present in QBDT
     * but not in PSP_EMPLOYEE table yet. These are created by PAPI call and will act as placeholder
     * until the data sync happens from QBDT to PSP.
     * @param pCompany
     * @return
     */
    public static DomainEntitySet<VmpEmployeeInfo> findVmpEmployeesInfo(Company pCompany) {
        Expression<VmpEmployeeInfo> query =
                new Query<VmpEmployeeInfo>()
                        .Where(VmpEmployeeInfo.Company().equalTo(pCompany));

        return Application.find(VmpEmployeeInfo.class, query);
    }

}