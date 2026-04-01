/*
 * Created on Apr 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
 * @author rgroth
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SpcfClassCreateInstanceException extends SpcfPortabilityModuleException
{
	private static final long serialVersionUID = 3200597002484437327L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ClassCreateInstance.getId());
	}

    /**
     * 
     */
    public SpcfClassCreateInstanceException()
    {
        super(sMessage);
    }

    /**
     * @param message
     */
    public SpcfClassCreateInstanceException(String message)
    {
        super(message);
    }

    /**
     * @param message
     * @param e
     */
    public SpcfClassCreateInstanceException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * @param e
     */
    public SpcfClassCreateInstanceException(Throwable e)
    {
        super(e);
    }
    
    /**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfClassCreateInstanceException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfClassCreateInstanceException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfClassCreateInstanceException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfClassCreateInstanceException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
    
    /**
     * We have to implement this method in order to uniquely identify
 	 * this error within the module in a type safe manner
 	 * @return A SpcfPortabilityErrorEnum number.
 	 */
 	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
 	{
 		return SpcfPortabilityErrorEnum.ClassCreateInstance;
 	}
}
