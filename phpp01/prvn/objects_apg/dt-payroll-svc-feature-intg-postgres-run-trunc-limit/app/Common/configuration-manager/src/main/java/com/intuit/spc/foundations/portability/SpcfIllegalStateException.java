package com.intuit.spc.foundations.portability;

/**
 * Portable Exception class.
 */
public class SpcfIllegalStateException extends SpcfPortabilityModuleException
{    

	private static final long serialVersionUID = 5252717068877776532L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.IllegalState.getId());
	}

	/**
     * Class constructor
     */
    public SpcfIllegalStateException()
    {
        super(sMessage);
    }
    
	/**
     * Constructs an instance with a detailed error message
     * @param message The detailed error message
     */
    public SpcfIllegalStateException(String message)
    {
        super(message);
    }

    /**
     * Constructs an instance with a detailed error message and cause
     * @param message The detailed error message
     * @param e The exception that caused this exception
     */
    public SpcfIllegalStateException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * Constructs an instance with a cause
     * 
     * @param e The exception that caused this exception
     */
    public SpcfIllegalStateException(Throwable e)
    {
        super(e);
    }
    
    /**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalStateException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfIllegalStateException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfIllegalStateException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfIllegalStateException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}

    /**
	 * @see com.intuit.spc.foundations.portability.SpcfPortabilityModuleException#getSpcfPortabilityError()
     */
    protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
    {       
        return SpcfPortabilityErrorEnum.IllegalState;
    }
}
