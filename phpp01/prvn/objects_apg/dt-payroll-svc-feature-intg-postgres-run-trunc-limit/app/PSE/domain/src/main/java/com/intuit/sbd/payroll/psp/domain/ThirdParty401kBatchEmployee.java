package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class ThirdParty401kBatchEmployee extends BaseThirdParty401kBatchEmployee {

	/**
	 * Default constructor.
	 */
	public ThirdParty401kBatchEmployee()
	{
		super();
	}

    public static DomainEntitySet<ThirdParty401kBatchEmployee> findTP401kEmployeeBatchByEmployee(Employee pEmployee) {
        Expression<ThirdParty401kBatchEmployee> query = new Query<ThirdParty401kBatchEmployee>()
                .Where(ThirdParty401kBatchEmployee.Employee().equalTo(pEmployee));

        return Application.find(ThirdParty401kBatchEmployee.class, query);            
    }

}