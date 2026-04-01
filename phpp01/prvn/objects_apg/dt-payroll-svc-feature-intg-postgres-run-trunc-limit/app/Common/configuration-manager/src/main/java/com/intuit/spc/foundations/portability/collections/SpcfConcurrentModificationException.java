package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
 * Portable exception class.
 *  System.InvalidOperationException
 *  java.util.ConcurrentModificationException 
 */
public class SpcfConcurrentModificationException extends SpcfPortabilityModuleException 
{
	private static final long serialVersionUID = -648566403351849094L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ConcurrentModification.getId());
	}
	
	/**
	 * Class constructor
	 */
	public SpcfConcurrentModificationException() { super(sMessage); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfConcurrentModificationException(String message) { super(message); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfConcurrentModificationException(String message, Throwable e) { super(message, e); }
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfConcurrentModificationException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfConcurrentModificationException(int applicationId, int architectureId, int moduleId) 
	{ 
		super(sMessage, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfConcurrentModificationException(String message, int applicationId, int architectureId, int moduleId) 
	{ 
		super(message, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfConcurrentModificationException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(message, e, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfConcurrentModificationException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.ConcurrentModification;
	}
}
