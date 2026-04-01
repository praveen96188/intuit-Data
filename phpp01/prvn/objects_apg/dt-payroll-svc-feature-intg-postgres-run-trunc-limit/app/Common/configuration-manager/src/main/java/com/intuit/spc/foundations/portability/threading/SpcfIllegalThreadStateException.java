package com.intuit.spc.foundations.portability.threading;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/***
 * 
 * SpcfIllegalThreadStateException class is thrown when a thread attempts to enter an 
 * incorrect state. An an example, this will can occur when a method of 
 * SpcfThread has been called more than once when it should be only called once.
 *
 */
public class SpcfIllegalThreadStateException extends SpcfPortabilityModuleException
{

	private static final long serialVersionUID = 2202242037455352495L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ThreadStateError.getId());
	}

	/**
	 * Class constructor
	 */
	public SpcfIllegalThreadStateException()
    {
        super(sMessage);
    }
	
	/**
     * Constructs an instance with a detailed error message
     * @param message The detailed error message
     */
    public SpcfIllegalThreadStateException(String message)
    {
        super(message);
    }

    /**
     * Constructs an instance with a detailed error message and cause
     * @param message The detailed error message
     * @param e The exception that caused this exception
     */
    public SpcfIllegalThreadStateException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * Constructs an instance with a cause.
     * 
     * @param e The exception that caused this exception
     */
    public SpcfIllegalThreadStateException(Throwable e)
    {
        super(e);
    }
    
    /**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalThreadStateException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfIllegalThreadStateException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfIllegalThreadStateException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfIllegalThreadStateException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}

    /**
     * Returns the enumeration for this SpcfPortabilityError
     * @return The SpcfPortabilityErrorEnum
     */
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError() 
	{
		return SpcfPortabilityErrorEnum.ThreadStateError;	
	}
}
