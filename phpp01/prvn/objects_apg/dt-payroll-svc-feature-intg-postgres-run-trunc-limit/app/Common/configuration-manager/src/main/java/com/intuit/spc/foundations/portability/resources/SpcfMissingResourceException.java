package com.intuit.spc.foundations.portability.resources;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
 * Portable exception class.
 *  System.Resources.MissingManifestResourceException 
 *  java.util.MissingResourceException 
 */
public class SpcfMissingResourceException extends SpcfPortabilityModuleException
{
	private static final long serialVersionUID = -4234324412012624093L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.MissingResource.getId());
	}

    /**
     * 
     */
    public SpcfMissingResourceException()
    {
        super(sMessage);
    }

    /**
     * @param message
     */
    public SpcfMissingResourceException(String message)
    {
        super(message);
    }

    /**
     * @param message
     * @param e
     */
    public SpcfMissingResourceException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * @param e
     */
    public SpcfMissingResourceException(Throwable e)
    {
        super(e);
    }
    
    /**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfMissingResourceException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfMissingResourceException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfMissingResourceException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfMissingResourceException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
    
    /**
     * We have to implement this method in order to uniquely identify
 	* this error within the module in a type safe manner
 	*/
 	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
 	{
 		return SpcfPortabilityErrorEnum.MissingResource;
 	}
}
