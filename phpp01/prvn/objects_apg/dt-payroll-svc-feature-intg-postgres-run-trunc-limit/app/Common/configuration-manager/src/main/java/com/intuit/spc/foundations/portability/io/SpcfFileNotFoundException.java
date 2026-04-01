package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;

/**
 * Portable checked exception class.
 * Signals that an attempt to open the file denoted by a 
 * specified pathname has failed. 
 */
public class SpcfFileNotFoundException extends SpcfPortabilityModuleException 
{
	private static final long serialVersionUID = -3893243310219994688L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.FileNotFound.getId());
	}
	
	/**
	 * Class constructor
	 */
	public SpcfFileNotFoundException() { super(sMessage); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfFileNotFoundException(String message) { super(message); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfFileNotFoundException(String message, Throwable e) { super(message, e); }
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfFileNotFoundException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfFileNotFoundException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfFileNotFoundException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfFileNotFoundException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfFileNotFoundException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.FileNotFound;
	}
}
