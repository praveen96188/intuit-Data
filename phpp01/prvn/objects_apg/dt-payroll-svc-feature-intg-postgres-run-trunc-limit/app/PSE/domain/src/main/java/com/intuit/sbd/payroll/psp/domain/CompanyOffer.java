package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.PSPDate;

/**
 * Hand-written business logic
 */
public class CompanyOffer extends BaseCompanyOffer {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public CompanyOffer()
	{
		super();
	}

    /**
     * Determines whether a claimed offer is active as of the PSPDate
     * @return
     */
    public boolean companyOfferIsActive() {
        if (getBeginDate() == null && getOffer().getBeginEvent()==OfferBeginEvent.FirstUseEvent) {
            return true;
        }

        if (getBeginDate() == null)
            return false;

        // if the begin-date is in the future... not active
        if (getBeginDate().after(PSPDate.getPSPTime()))
            return false;

        // if the end-date is in the past... not active
        if (getEndDate()!=null && getEndDate().before(PSPDate.getPSPTime()))
            return false;

        // if it's usage-limited and there are no more usages... not active
        if (getOffer().getEndEvent()==OfferEndEvent.PayrollUsageEvent && getUsagesRemaining() <= 0)
            return false;

        // it's active
        return true;
    }
}