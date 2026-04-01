package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Hand-written business logic
 */
public class OfferingServiceChargePrice extends BaseOfferingServiceChargePrice {
    private static SpcfMoney ZERO = new SpcfMoney("0.00");

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public OfferingServiceChargePrice()
	{
		super();
	}
    
    public SpcfMoney calcTierAdjustedFees(int pQuantity) {
        int tierUnitOffset = getOfferingServiceCharge().getTierUnits();
        int tierAdjustedQuantity = pQuantity;

        //
        // Adjust the unit quantity by the selected Tier (if applicable)
        //
        if (tierUnitOffset > 0) {
            //
            // The expectation here is that the tier has already been selected based on quantity.
            // (example: If Tier2 starts at 11 units, we want to subtract off the first 10...)
            //
            tierAdjustedQuantity -= (tierUnitOffset - 1);
        }

        if (tierAdjustedQuantity < 0) {
            tierAdjustedQuantity = 0;
        }

        //
        // Calculate the total fees based on units (adjusting for tier)
        //
        SpcfDecimal unitPrice = getUnitPrice();

        if (unitPrice == null) {
            unitPrice = ZERO;
        }

        SpcfDecimal itemTotal = SpcfDecimal.createInstance(tierAdjustedQuantity).multiply(unitPrice);

        //
        // Then add on the base price for the tier
        //
        SpcfDecimal basePrice = getBasePrice();

        if (basePrice == null) {
            basePrice = ZERO;
        }

        itemTotal = itemTotal.add(basePrice);

        return new SpcfMoney(itemTotal);
    }
}
