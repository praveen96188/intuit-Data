package com.intuit.spc.foundations.portability;

/**
 * Portable exception class.
 *  System.SpcfNullReferenceException
 *  java.lang.NullPointerException 
 */
public class SpcfIndexOutOfBoundsException extends SpcfPortabilityModuleException 
{
	private static final long serialVersionUID = -4751072765992689564L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.IndexOutOfBounds.getId());
	}
	
	/**
	 * Class constructor
	 */
	public SpcfIndexOutOfBoundsException() { super(sMessage); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfIndexOutOfBoundsException(String message) { super(message); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfIndexOutOfBoundsException(String message, Throwable e) { super(message, e); }
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfIndexOutOfBoundsException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIndexOutOfBoundsException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfIndexOutOfBoundsException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfIndexOutOfBoundsException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfIndexOutOfBoundsException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.IndexOutOfBounds;
	}
}
