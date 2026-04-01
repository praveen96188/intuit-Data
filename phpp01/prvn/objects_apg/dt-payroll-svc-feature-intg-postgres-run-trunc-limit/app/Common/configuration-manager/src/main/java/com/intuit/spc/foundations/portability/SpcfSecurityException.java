package com.intuit.spc.foundations.portability;

/**
 * Portable exception class.
 *  java.lang.SecurityException 
 */
public class SpcfSecurityException extends SpcfPortabilityModuleException 
{
	private static final long serialVersionUID = -6822032405953100660L;
	private static String sMessage;
	private SpcfPortabilityErrorEnum errorEnum;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.Security.getId());
	}
	
	/**
	 * Class constructor
	 */
	public SpcfSecurityException() { super(sMessage); }
	
	/**
	 * Constructor with message and Portablity Error Enum.
	 * @param message Error message.
	 * @param errorEnum Error number is defined in this object.
	 */
	public SpcfSecurityException(String message, SpcfPortabilityErrorEnum errorEnum)
	{
		super(message);
		this.errorEnum = errorEnum;
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfSecurityException(String message) { super(message); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfSecurityException(String message, Throwable e) { super(message, e); }
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfSecurityException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfSecurityException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfSecurityException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfSecurityException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfSecurityException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		if ( errorEnum != null )
			return errorEnum;
		else 
			return SpcfPortabilityErrorEnum.Security;
	}
	
}
