package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
 * This class is a portable exception class for invalid portable representation.
 * @author gwang1
 *
 */
public class SpcfInvalidTypeStringException 
	extends SpcfPortabilityModuleException 
{
	/**
	 * Eclipse generated serial version UID
	 */
	private static final long serialVersionUID = 2133182069117177224L;
	
	/**
	 * Default message
	 */
	private static String sMessage;
	
	static
	{
		sMessage = 
			getDefaultMessage(SpcfPortabilityErrorEnum.InvalidTypeString.getId());
	}

    /**
     * Default constructor. 
     */
    public SpcfInvalidTypeStringException()
    {
        super(sMessage);
    }

    /**
     * @param message
     */
    public SpcfInvalidTypeStringException(String message)
    {
        super(message);
    }

    /**
     * @param message
     * @param e
     */
    public SpcfInvalidTypeStringException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * @param e
     */
    public SpcfInvalidTypeStringException(Throwable e)
    {
        super(e);
    }
    
    /**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfInvalidTypeStringException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidTypeStringException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidTypeStringException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidTypeStringException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
    
    /**
     * We have to implement this method in order to uniquely identify
 	 * this error within the module in a type safe manner
 	 * @return A SpcfPortabilityErrorEnum number.
 	 */
	@Override
 	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
 	{
 		return SpcfPortabilityErrorEnum.InvalidTypeString;
 	}
}

