package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Hand-written business logic
 */
public class Offer extends BaseOffer {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Offer()
	{
		super();
	}

    /**
     * Finds an offer given an offer code.  Returns null if there is no such offer.
     * @param pOfferCd
     * @return
     */
    static public Offer findOfferByOfferCode(String pOfferCd) {
        DomainEntitySet<Offer> found = Application.find(Offer.class, OfferCd().equalTo(pOfferCd));
        if (found==null || found.isEmpty()) {
            return null;
        } else {
            return found.get(0);
        }
    }

    static public Offer findOfferByPromotionId(String pPromotionId) {
        DomainEntitySet<Offer> found = Application.find(Offer.class, PromotionId().equalTo(pPromotionId));
        if (found==null || found.isEmpty()) {
            return null;
        } else {
            return found.get(0);
        }
    }

    /**
     * Determines whether a claimed offer is applicable to a given charge
     */
    public boolean offerIsApplicable(OfferingServiceCharge pCharge) {
        if (getDiscountType() == DiscountType.AltPrice) {
            // this kind of offer is applicable if it has an alternate price for this type of fee
            OfferPrice price = getAlternatePrice(pCharge.getOfferingServiceChargeGroup().getAppliesTo());
            return (price != null); // found ==> applicable
        } else {
            // these types of offers are applicable if they refer specifically to this OfferingServiceCharge
            return getOfferingServiceChargeCollection().contains(pCharge);
        }
    }

    public OfferPrice getAlternatePrice(OfferingServiceChargeType pChargeType) {
        if (getDiscountType() != DiscountType.AltPrice) {
            return null;
        }

        DomainEntitySet<OfferPrice> found =
                Application.find(OfferPrice.class,
                                 OfferPrice.Offer().equalTo(this)
                                 .And(OfferPrice.FeeType().equalTo(pChargeType)));

        return found.isEmpty() ? null : found.get(0);
    }

    public SpcfDecimal getDiscount(SpcfDecimal pGrossAmount) {
        SpcfDecimal discount = SpcfMoney.ZERO;

        if (pGrossAmount != null) {
            switch (getDiscountType()) {
                case AltPrice: // alternate price (not a discount)
                    break;

                case PercentOff: // discount is a percentage of the gross amount
                    discount = pGrossAmount.multiply(SpcfDecimal.createInstance(getDiscountPercent() / 100.0));
                    break;

                default: // AmountOff - discount is a fixed amount
                    discount = getDiscountAmount();
                    break;
            }

            //
            // Ensure discount is non-null
            //
            if (discount == null) {
                discount = SpcfMoney.ZERO;
            }

            if (discount.isGreaterThan(SpcfMoney.ZERO)) {
                //
                // Ensure discount is rounded to nearest penny
                //
                discount = discount.setScale(2, SpcfDecimal.SpcfRoundingType.HalfUp);

                //
                // Ensure the application of the discount to the gross can never result in a negative value
                //
                if (pGrossAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
                    discount = SpcfMoney.ZERO;
                } else if (pGrossAmount.subtract(discount).isLessThan(SpcfMoney.ZERO)) {
                    discount = pGrossAmount;
                }
            }
        }

        return discount;
    }
}
