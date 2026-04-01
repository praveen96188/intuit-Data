package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class AnnualBillingItem extends BaseAnnualBillingItem {

	/**
	 * Default constructor.
	 */
	public AnnualBillingItem()
	{
		super();
	}

    public static DomainEntitySet<AnnualBillingItem> findPendingAnnualBillingItems(AnnualBillingBatch pAnnualBillingItem, AnnualBillingItemStatusCode... pAnnualBillingItemStatusCode) {
                Expression<AnnualBillingItem> query =
                        new Query<AnnualBillingItem>()
                            .Where(AnnualBillingItem.AnnualBillingBatch().equalTo(pAnnualBillingItem)
                                    .And(AnnualBillingItem.AnnualBillingItemStatusCd().in(pAnnualBillingItemStatusCode)));
        return Application.find(AnnualBillingItem.class, query);
    }

}