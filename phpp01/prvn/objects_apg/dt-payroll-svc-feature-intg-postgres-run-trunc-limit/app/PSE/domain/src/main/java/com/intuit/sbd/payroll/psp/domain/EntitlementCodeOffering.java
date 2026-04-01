package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class EntitlementCodeOffering extends BaseEntitlementCodeOffering {

	/**
	 * Default constructor.
	 */
	public EntitlementCodeOffering()
	{
		super();
	}

    public static DomainEntitySet<EntitlementCodeOffering> findEntitlementCodeOfferingsGroupByPriceType(String pAssetItemNumber) {
        Criterion<EntitlementCodeOffering> where = EntitlementCodeOffering.EntitlementCode().AssetItemNumber().equalTo(pAssetItemNumber);

        Expression<EntitlementCodeOffering> query =
                new Query<EntitlementCodeOffering>()
                       .Where(where).GroupBy(EntitlementCodeOffering.PriceType());

        return Application.find(EntitlementCodeOffering.class, query).sort(EntitlementCodeOffering.PriceType());
    }

}