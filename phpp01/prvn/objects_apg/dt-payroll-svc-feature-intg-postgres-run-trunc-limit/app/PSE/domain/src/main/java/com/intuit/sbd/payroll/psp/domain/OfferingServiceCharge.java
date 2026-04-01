package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Hand-written business logic
 */
public class OfferingServiceCharge extends BaseOfferingServiceCharge {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static OfferingServiceCharge findBySKU(String pSKU) {
        OfferingServiceCharge foundOfferingServiceCharge = null;

        NaturalKey naturalKey = new NaturalKey(OfferingServiceCharge.class, pSKU);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);


        if (primaryKey != null) {
            foundOfferingServiceCharge = Application.findById(OfferingServiceCharge.class, primaryKey);
        } else {
            DomainEntitySet<OfferingServiceCharge> found = Application.find(OfferingServiceCharge.class, SKU().equalTo(pSKU));
            if (found == null || found.size() == 0) {
                foundOfferingServiceCharge = null;
            } else {
                // todo is this right? sku is not unique in this table?? should we change the model to add a join table between service charge type and offering service charge group??
                foundOfferingServiceCharge = found.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, foundOfferingServiceCharge.getId());
            }
        }

        return foundOfferingServiceCharge;
    }

    public static OfferingServiceChargeType findOfferingServiceChargeTypeBySKU(String pSKU) {
        OfferingServiceCharge offeringServiceCharge = findBySKU(pSKU);
        if (offeringServiceCharge == null) {
            return null;
        } else {
            return offeringServiceCharge.getOfferingServiceChargeGroup().getAppliesTo();
        }
    }

    /**
     * Returns true if the given SKU is already in use by any OfferingServiceCharge or Offering entities.
     * If non-null, pExcluded (an OfferingServiceCharge) is excluded from the test.
     * @param offeringServiceCharge
     * @param pSKU The SKU to be tested
     * @return true if the SKU is in use (by any but the excluded entity), otherwise false
     */
    public static boolean skuIsInUse(String pSKU, OfferingServiceCharge offeringServiceCharge)
    {
        if (Offering.findBySKU(pSKU) != null) // if a service charge is using this sku
        {
            return true;
        }
        else // no service charge is using it
        {
            OfferingServiceCharge existing = findBySKU(pSKU);
            if (existing == null) // no offering is using it
                return false;
            else if (offeringServiceCharge !=null && offeringServiceCharge.equals(existing)) // in use by the excluded offering
                return false;
            else // in use by one that is NOT excluded
                return true;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public OfferingServiceCharge() {
		super();
	}

    public OfferingServiceChargePrice getCurrentPrice() {
        return getEffectivePrice(PSPDate.getPSPTime());
    }

    /**
     * For a given OfferingServiceCharge, return the OfferingServiceChargePrice that was current as of the given date.
     * @param pDateTime The date for which you want the then-current price
     * @return the OfferingServiceChargePrice that was/is/will-be effective on the given date, or null
     */
    public OfferingServiceChargePrice getEffectivePrice(SpcfCalendar pDateTime) {
        // the where clause selects prices with effective dates not in the future
        // order by EffectiveDate (descending) and ModifiedDate (descending)... if two price entities have the
        // same EffectiveDate, we take the one that was created/modified most recently as the intendend one
        Expression<OfferingServiceChargePrice> query =
                new Query<OfferingServiceChargePrice>()
                        .Where(OfferingServiceChargePrice.OfferingServiceCharge().equalTo(this)
                                                         .And(OfferingServiceChargePrice.EffectiveDate().lessOrEqualThan(pDateTime)))
                        .OrderBy(OfferingServiceChargePrice.EffectiveDate().Descending(), ModifiedDate().Descending());

        DomainEntitySet<OfferingServiceChargePrice> prices = Application.find(OfferingServiceChargePrice.class, query);
        
        return prices.isEmpty() ? null : prices.get(0);
    }

    public SpcfMoney calcTierAdjustedFees(int pQuantity) {
        OfferingServiceChargePrice price = getCurrentPrice();
        return (price == null) ? SpcfMoney.ZERO : price.calcTierAdjustedFees(pQuantity);
    }
}
