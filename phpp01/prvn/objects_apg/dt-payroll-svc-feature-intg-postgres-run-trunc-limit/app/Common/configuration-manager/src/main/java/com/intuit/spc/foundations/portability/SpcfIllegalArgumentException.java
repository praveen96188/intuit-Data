package com.intuit.spc.foundations.portability;

/**
 * Portable exception class.
 *  System.ArgumentException
 *  java.lang.IllegalArgumentException 
 */
public class SpcfIllegalArgumentException extends SpcfPortabilityModuleException 
{
	private static final long serialVersionUID = 1646010767094118449L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.IllegalArgument.getId());
	}
	
	/**
	 * Class constructor
	 */
	public SpcfIllegalArgumentException() 
	{ 
		super(sMessage); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfIllegalArgumentException(String message) 
	{ 
		super(message); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfIllegalArgumentException(String message, Throwable e) 
	{ 
		super(message, e); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfIllegalArgumentException(Throwable e) 
	{ 
		super(e); 
	}
	
	/**
     * constructor.
 	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
     */
	public SpcfIllegalArgumentException(int applicationId, int architectureId, int moduleId) 
	{
		super(sMessage, applicationId, architectureId, moduleId);
	}

	/**
	 * Constructs an exception with a message.
	 * @param message The exception message.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalArgumentException(String message, int applicationId, int architectureId, int moduleId)
	{
		super(message, applicationId, architectureId, moduleId);
	}

	/**
	 *  Constructs an exception with a message and chained exception.
	 *  @param message The exception message.
	 *  @param e A throwable exception.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalArgumentException(String message, Throwable e, int applicationId, int architectureId, int moduleId)
	{
		super(message, e, applicationId, architectureId, moduleId);	
	}

	/**
	 * Constructs with a chained exception.
	 * Consistency - This constructor was provided to augment C# 
	 * capability.
	 * @param e A throwable exception.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalArgumentException(Throwable e, int applicationId, int architectureId, int moduleId)
	{
		super(e, applicationId, architectureId, moduleId);	
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.IllegalArgument;
	}
}
