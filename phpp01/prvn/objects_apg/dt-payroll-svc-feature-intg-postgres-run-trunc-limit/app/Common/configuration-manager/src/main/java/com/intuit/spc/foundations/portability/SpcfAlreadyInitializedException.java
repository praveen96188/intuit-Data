package com.intuit.spc.foundations.portability;

/**
 * Portable exception class. Exception is thrown when an argument is attempted to be unnecessarily re-initialized.
 * @author ggrad
 *
 */
public class SpcfAlreadyInitializedException extends SpcfPortabilityModuleException 
{
	private static final long serialVersionUID = -1706689740872001907L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.AlreadyInitialized.getId());
	}
	
	private static String getErrorMessage(String argumentName)
	{
		return sMessage + " Argument: " + argumentName;
	}
	
	/**
	 * Class constructor
	 */
	public SpcfAlreadyInitializedException() { super(sMessage); }
	
	/**
	 * Class constructor
	 * @param argumentName An exception message
	 */
	public SpcfAlreadyInitializedException(String argumentName) 
	{ 
		super(getErrorMessage(argumentName)); 
	}
	
	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 * @param e A throwable exception
	 */
	public SpcfAlreadyInitializedException(String argumentName, Throwable e) 
	{ 
		super(getErrorMessage(argumentName), e); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfAlreadyInitializedException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfAlreadyInitializedException(int applicationId, int architectureId, int moduleId) 
	{ 
		super(sMessage, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfAlreadyInitializedException(String argumentName, int applicationId, int architectureId, int moduleId) 
	{ 
		super(getErrorMessage(argumentName), applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfAlreadyInitializedException(String argumentName, Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(getErrorMessage(argumentName), e, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfAlreadyInitializedException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.AlreadyInitialized;
	}
}
