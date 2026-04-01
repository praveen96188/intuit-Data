package com.intuit.spc.foundations.portability.threading;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/***
 * 
 * SpcfThreadInterruptedException class is thrown by portability threading
 * classes when the current thread enters a blocking call and the interrupt
 * status is set or if a thread is interrupted during the blocking call. 
 * 
 * It is the blocking thread that receives this exception, not the client 
 * requesting the interrupt except if a thread interrupts itself.
 * 
 * Blocking calls are thread sleep, join or wait. 
 * 
 */
public class SpcfThreadInterruptedException  extends SpcfPortabilityModuleException
{

	private static final long serialVersionUID = 1723295126422184234L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ThreadInterruptedError.getId());
	}

	/**
	 * Class constructor
     */
    public SpcfThreadInterruptedException()
    {
        super(sMessage);
    }
    
	/**
     * Constructs an instance with a detailed error message
     * @param message The detailed error message
     */
    public SpcfThreadInterruptedException(String message)
    {
        super(message);
    }

    /**
     * Constructs an instance with a detailed error message and cause
     * @param message The detailed error message
     * @param e The exception that caused this exception
     */
    public SpcfThreadInterruptedException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * Constructs an instance with a cause.
     * 
     * @param e The exception that caused this exception
     */
    public SpcfThreadInterruptedException(Throwable e)
    {
        super(e);
    }
    
    /**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfThreadInterruptedException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfThreadInterruptedException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfThreadInterruptedException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfThreadInterruptedException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
    /**
     * Returns the SpcfPortabilityErrorEnum
     */
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError() 
	{
		return SpcfPortabilityErrorEnum.ThreadInterruptedError;	
	}
}
