package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class AnnualBillingBatch extends BaseAnnualBillingBatch {

	/**
	 * Default constructor.
	 */
	public AnnualBillingBatch()
	{
		super();
	}

    public static AnnualBillingBatch findAnnualBillingBatch(FormTypeCode pFormTypeCode, Integer pFormYear) {
                Expression<AnnualBillingBatch> query =
                        new Query<AnnualBillingBatch>()
                            .Where(AnnualBillingBatch.FormTypeCd().equalTo(pFormTypeCode)
                               .And(AnnualBillingBatch.FormYear().equalTo(pFormYear)));

        return Application.find(AnnualBillingBatch.class, query).getFirst();
    }

}