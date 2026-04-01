package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class ThirdParty401kBatchPaycheck extends BaseThirdParty401kBatchPaycheck {

	/**
	 * Default constructor.
	 */
	public ThirdParty401kBatchPaycheck()
	{
		super();
	}

    public static ThirdParty401kBatchPaycheck findThirdParty401kBatchPaycheck(Paycheck pPaycheck) {
        Expression<ThirdParty401kBatchPaycheck> query =
                new Query<ThirdParty401kBatchPaycheck>()
                        .Where(ThirdParty401kBatchPaycheck.Paycheck().Id().equalTo(pPaycheck.getId()));

        DomainEntitySet<ThirdParty401kBatchPaycheck> returnSet = Application.find(ThirdParty401kBatchPaycheck.class, query);

        return returnSet.isEmpty() ? null : returnSet.get(0);
    }

}