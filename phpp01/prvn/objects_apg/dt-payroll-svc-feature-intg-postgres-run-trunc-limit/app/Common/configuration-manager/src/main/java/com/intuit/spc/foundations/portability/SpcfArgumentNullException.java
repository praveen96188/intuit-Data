package com.intuit.spc.foundations.portability;

/**
 * Portable exception class
 */
public class SpcfArgumentNullException extends SpcfIllegalArgumentException 
{

	private static final long serialVersionUID = 20237686624299104L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ArgumentNull.getId());
	}
	
	private static String getErrorMessage(String argumentName)
	{
		return sMessage + " Argument: " + argumentName;
	}

	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 */
	public SpcfArgumentNullException(String argumentName)
	{
		super(getErrorMessage(argumentName));
	}
	
	/**
	 * Class constructor
	 * @param ex A throwable exception
	 */
	public SpcfArgumentNullException(Exception ex)
	{
		super(ex);
	}
	
	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArgumentNullException(String argumentName, int applicationId, int architectureId, int moduleId) 
	{ 
		super(getErrorMessage(argumentName), applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArgumentNullException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}

	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.ArgumentNull;
	}
}