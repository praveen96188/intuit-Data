package com.intuit.spc.foundations.portability;

/**
 * Portable exception class
 */
public class SpcfInvalidFactoryStateException extends SpcfPortabilityModuleException
{
	private static final long serialVersionUID = -4632784395166753893L;
	
	// don't call getDefaultMessage() in this case as it makes an impl call 
	// and it is thrown in the case where portabilityspecific can't be loaded
	private static String sMessage = "Invalid factory state.";

    /**
     * default constructor
     */
    public SpcfInvalidFactoryStateException()
    {
        super(sMessage);
    }

    /**
     * Constructor which will take a message as a parameter
     * @param message
     */
    public SpcfInvalidFactoryStateException(String message)
    {
        super(message);
    }

    /**
     * Class constructor
     * @param message
     * @param e
     */
    public SpcfInvalidFactoryStateException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * Class constructor
     * @param e
     */
    public SpcfInvalidFactoryStateException(Throwable e)
    {
        super(e);
    }
    
    /**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfInvalidFactoryStateException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidFactoryStateException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidFactoryStateException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidFactoryStateException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
    
    /**
     * We have to implement this method in order to uniquely identify
 	* this error within the module in a type safe manner
 	*/
 	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
 	{
 		return SpcfPortabilityErrorEnum.InvalidFactoryState;
 	}
}
