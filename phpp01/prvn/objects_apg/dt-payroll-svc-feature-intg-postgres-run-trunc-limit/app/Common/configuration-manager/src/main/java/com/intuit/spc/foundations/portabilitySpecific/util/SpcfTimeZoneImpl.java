package com.intuit.spc.foundations.portabilitySpecific.util;

import java.util.TimeZone;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Platform specific implementation of SpcfTimeZone.
 */
public class SpcfTimeZoneImpl extends SpcfTimeZone
{
	
	
	
	private TimeZone mTimeZone;
	
	/**
	 * Default constructor which will create local time-zone.
	 */
	public SpcfTimeZoneImpl()
	{	
		mTimeZone = TimeZone.getDefault();
	}
	
	/**
	 * Constructs  an object of SpcfTimeZoneImpl from the platform specific TimeZone object.
	 * @param zone platform specific time-zone object
	 * @throws SpcfArgumentNullException if zone is null
	 */
	public SpcfTimeZoneImpl(TimeZone zone)
	{	
		SpcfParamValidator.checkIsNotNull(zone, "zone");		
		mTimeZone = zone;
	}
	
	/**
     * @see SpcfTimeZone#getStandardName()
     */
	public String getStandardName()
	{
		return mTimeZone.getDisplayName(false, TimeZone.LONG);
	}
	
	/**
     * @see SpcfTimeZone#getDaylightName()
     */
	public String getDaylightName()
	{
		return mTimeZone.getDisplayName(true, TimeZone.LONG);
	}	
	
	/**
     * @see SpcfTimeZone#getOffset(long)
     */
	public int getOffset(long date) 
	{	
		if( date < SpcfCalendar.MinMillisecond  || date > SpcfCalendar.MaxMillisecond)
		{
			throw new SpcfIllegalArgumentException("date is out of range.");
		}
		return mTimeZone.getOffset(date);
	}
    
    
	
	/**
	* Returns the encapsulated third party runtime object. 
	* 
	* @return a System.TimeZone implementation
	*/
	public TimeZone toSpecific()
	{
		return mTimeZone;
	}

    @Override
    public void setTimeZone(String id)
    {
        mTimeZone = TimeZone.getTimeZone(id);
    }
}
