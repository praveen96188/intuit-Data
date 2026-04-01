package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class ThirdParty401kPaycheckPendingState extends BaseThirdParty401kPaycheckPendingState {

	public static DomainEntitySet<ThirdParty401kPaycheckPendingState> findOnHoldPaycheckByInitiationDate(SpcfCalendar pInitiationDateStart, SpcfCalendar pInitiationDateEnd) {
        String[] paramNames = new String[2];
        paramNames[0] = "initiationDateStart";
        paramNames[1] = "initiationDateEnd";

        Object[] paramValues = new Object[2];
        paramValues[0] = pInitiationDateStart;
        paramValues[1] = pInitiationDateEnd;

        return Application.findByNamedQuery("findOnHoldPaycheckByInitiationDate", paramNames, paramValues);
    }


	/**
	 * Default constructor.
	 */
	public ThirdParty401kPaycheckPendingState()
	{
		super();
	}

    public String toString() {
        return super.toString() + "  StateCd: " + getStateCd() + "  Initiation Date: " + getInitiationDate();
    }

}