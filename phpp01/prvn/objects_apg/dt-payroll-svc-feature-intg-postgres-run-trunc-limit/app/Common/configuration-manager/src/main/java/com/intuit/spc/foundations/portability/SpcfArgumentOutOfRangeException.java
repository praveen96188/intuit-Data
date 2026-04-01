package com.intuit.spc.foundations.portability;

/**
 * Portable exception class
 */
public class SpcfArgumentOutOfRangeException extends SpcfIllegalArgumentException 
{

	private static final long serialVersionUID = 1332131285475222117L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ArgumentOutOfRange.getId());
	}
	
	/**
	 * Creates a message describing the exception.
	 * @param argumentName The name of the argument out of range.
	 * @param argumentValue The value of the argument out of range.
	 * @return The string message.
	 */
	private static String getErrorMessage(String argumentName, Object argumentValue)
	{
		return sMessage + " Argument: " + argumentName + " Value: "  + argumentValue +".";
	}

	/**
	 * Class constructor
	 * @param argumentName The name of the argument out of range.
	 * @param argumentValue The value of the argument out of range.
	 */
	public SpcfArgumentOutOfRangeException(String argumentName, String argumentValue) 
	{ 
		super(getErrorMessage(argumentName, argumentValue)); 
	}
	
	/**
	 * Class constructor
	 */
	public SpcfArgumentOutOfRangeException() 
	{ 
		super(sMessage); 
	}
	
	/**
	 * Class constructor
	 * @param argumentName The name of the exception's argument
	 * @param argumentValue The value of the argument out of range.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArgumentOutOfRangeException(String argumentName, String argumentValue, int applicationId, int architectureId, int moduleId) 
	{ 
		super(getErrorMessage(argumentName, argumentValue), applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArgumentOutOfRangeException(int applicationId, int architectureId, int moduleId) 
	{ 
		super(sMessage, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.ArgumentOutOfRange;
	}
}