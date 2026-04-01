package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class HoursWorkedException extends BaseHoursWorkedException {

    static String cacheKey = "CachedHoursWorkedExceptions";
	/**
	 * Default constructor.
	 */
	public HoursWorkedException()
	{
		super();
	}

    public static DomainEntitySet<HoursWorkedException> findHoursWorkedExceptionsByLaw(String pLawId) {

        DomainEntitySet<HoursWorkedException> hoursWorkedExceptions;
        if (Application.getSessionCache().isDataObjectCollectionCached(HoursWorkedException.class, cacheKey)) {
            hoursWorkedExceptions = Application.getSessionCache().getDataObjectCollection(HoursWorkedException.class, cacheKey);
        } else {
            hoursWorkedExceptions = Application.findObjects(HoursWorkedException.class);
            Application.getSessionCache().addDataObjectCollection(HoursWorkedException.class, cacheKey, hoursWorkedExceptions);
        }

        return hoursWorkedExceptions.find(HoursWorkedException.Law().LawId().equalTo(pLawId));
    }

}