package com.intuit.spc.foundations.portability;

/**
 * Portable exception class. Exception is thrown when an argument is not initialized as expected.
 * @author ggrad
 *
 */
public class SpcfNotInitializedException extends SpcfPortabilityModuleException
{
	private static final long serialVersionUID = -8712876780626512124L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.NotInitialized.getId());
	}
	
	private static String getErrorMessage(String argumentName)
	{
		return sMessage + " Argument: " + argumentName;
	}
	
	/**
	 * Class constructor
	 */
	public SpcfNotInitializedException() { super(sMessage); }
	
	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 */
	public SpcfNotInitializedException(String argumentName) 
	{ 
		super(getErrorMessage(argumentName)); 
	}
	
	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 * @param e A throwable exception
	 */
	public SpcfNotInitializedException(String argumentName, Throwable e) 
	{ 
		super(getErrorMessage(argumentName), e); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfNotInitializedException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfNotInitializedException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfNotInitializedException(String argumentName, int applicationId, int architectureId, int moduleId) 
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
	public SpcfNotInitializedException(String argumentName, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfNotInitializedException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.NotInitialized;
	}
}
