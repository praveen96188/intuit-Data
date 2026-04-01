package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
* Portable exception class.
* NoSuchFielddException 
*/
public class SpcfClassNoSuchFieldException extends SpcfPortabilityModuleException {
	
	private static final long serialVersionUID = 358385288115273572L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.NoSuchField.getId());
	}
	
	/**
	 * Class constructor
	 */
	public SpcfClassNoSuchFieldException() 
	{ 
		super(sMessage); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfClassNoSuchFieldException(String message) 
	{
		super(message); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfClassNoSuchFieldException(String message, Throwable e) { 
		super(message, e); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfClassNoSuchFieldException(Throwable e) 
	{ 
		super(e); 
	}
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfClassNoSuchFieldException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfClassNoSuchFieldException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfClassNoSuchFieldException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfClassNoSuchFieldException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
     * Get SpcfPortabilityError, uniquely identifying this error within 
     * the module in a type safe manner.
     * @return A SpcfPortabilityErrorEnum number.
	 */
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError(){
		return SpcfPortabilityErrorEnum.NoSuchField;
	}
}
