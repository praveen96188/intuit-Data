
package com.intuit.spc.foundations.portability;


import com.intuit.spc.foundations.portability.resources.SpcfResourceManager;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.reflect.SpcfPortabilityResolver;

/**
 * A class for describing exceptions related to the portability module.
 */
public abstract class SpcfPortabilityModuleException extends SpcfArchitectureException {

	private static final long serialVersionUID = 1L;
	
    /**
     * The default constructor.
     */
	public SpcfPortabilityModuleException() {}

	/**
	 * Constructs an exception with a message.
	 * @param message The exception message.
	 */
	public SpcfPortabilityModuleException(String message)
	{
		super(message);
	}

	/**
	 *  Constructs an exception with a message and chained exception.
	 *  @param message The exception message.
	 *  @param e A throwable exception.
	 */
	public SpcfPortabilityModuleException(String message, Throwable e)
	{
		super(message, e);	
	}

	/**
	 * Constructs with a chained exception.
	 * Consistency - This constructor was provided to augment C# 
	 * capability.
	 * @param e A throwable exception.
	 */
	public SpcfPortabilityModuleException(Throwable e)
	{
		super(e);	
	}
	
	/**
     * constructor.
 	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
     */
	public SpcfPortabilityModuleException(int applicationId, int architectureId, int moduleId) {}

	/**
	 * Constructs an exception with a message.
	 * @param message The exception message.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfPortabilityModuleException(String message, int applicationId, int architectureId, int moduleId)
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
	public SpcfPortabilityModuleException(String message, Throwable e, int applicationId, int architectureId, int moduleId)
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
	public SpcfPortabilityModuleException(Throwable e, int applicationId, int architectureId, int moduleId)
	{
		super(e, applicationId, architectureId, moduleId);	
	}

	/**
	 * Your SpcfXXXException must add a unique entry to the 
	 * PortabilityErrorEnum and return it when it overrides this 
	 * method. This is how we ensure our exceptions are using ids 
	 * unique to the module in a type safe manner.
	 * @return An SpcfPortabilityErrorEnum
	 */
	protected abstract SpcfPortabilityErrorEnum getSpcfPortabilityError();
	

	/**
	 * Override the generic, error prone int, to call the typesafe
	 * PortabilityError property we're going to make everyone implement.
	 * @return The error ID. 
	 */
	protected int getErrorIdForDefiningType()
	{
		return getSpcfPortabilityError().getId();
	}

	/**
	 * We have to implement this method in order to uniquely identify
	 * this module within the architecture in a type safe manner
	 * @return An SpcfArchitectureModuleEnum.
	 */
	protected SpcfArchitectureModuleEnum getSpcfArchitectureModule()
	{
		return SpcfArchitectureModuleEnum.Portability;
	}
	
	/**
	 * 
	 * @param messageId An integer corresponding to an exception id in the portability error enum
	 * @return A string representing the default message for the identified exception
	 */
	public static String getDefaultMessage(int messageId)
	{
		String messageIdString = "" + messageId;
		String defaultMessage = "";
		
		try
		{
			String resourceName = SpcfPortabilityResolver.translateTypeFullName(
				"com.intuit.spc.foundations.portability.SpcfPortabilityExceptionStrings");
			
			String assemblyFullName = SpcfClass.createAssemblyFullName(
				"Intuit.Spc.Foundations.Portability", SpcfPortabilityModuleException.class);
		
			SpcfResourceManager exceptionMessages = SpcfResourceManager.createInstance(resourceName, assemblyFullName);
			defaultMessage = exceptionMessages.getString(messageIdString);
		}
		catch(Exception ex)
		{
			// don't confuse the originating exception with tangential exceptions. just put a warning in the message.
			defaultMessage = 
				"Could not retrieve exception message from SpcfPortabilityExceptionStrings due to the following error:"
				+ ex.getMessage();
		}
		
		return defaultMessage;
	}
}
