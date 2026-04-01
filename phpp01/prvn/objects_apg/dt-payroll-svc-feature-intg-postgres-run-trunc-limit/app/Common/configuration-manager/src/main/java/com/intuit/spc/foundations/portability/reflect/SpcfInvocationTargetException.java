package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
 * Portable exception class.
 * InvocationTargetException
 * 
 * @author gwang
 */
public class SpcfInvocationTargetException extends SpcfPortabilityModuleException 
{
	/**
	 * Eclipse generated serial version UID
	 */
	private static final long serialVersionUID = 2133182069117177223L;


	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfInvocationTargetException(Throwable e) 
	{ 
		super(e); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfInvocationTargetException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}

	/**
	 * We have to implement this method in order to uniquely identify this error within the
	 * module in a type safe manner.
	 * @return A SpcfPortabilityErrorEnum number.
	 */
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.InvocationTarget;
	}
}
