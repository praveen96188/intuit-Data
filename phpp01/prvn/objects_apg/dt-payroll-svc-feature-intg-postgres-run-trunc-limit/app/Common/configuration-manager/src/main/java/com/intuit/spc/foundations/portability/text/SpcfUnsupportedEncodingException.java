package com.intuit.spc.foundations.portability.text;

import com.intuit.spc.foundations.portability.*;

/**
 * Portable exception class.
 * Signals that the encoding requested is not supported 
 */
public class SpcfUnsupportedEncodingException extends SpcfPortabilityModuleException 
{
	private static final long serialVersionUID = 3886523553316247897L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.UnsupportedEncoding.getId());
	}
	
	/**
	 * Class constructor
	 */
	public SpcfUnsupportedEncodingException() { super(sMessage); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfUnsupportedEncodingException(String message) { super(message); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfUnsupportedEncodingException(String message, Throwable e) { super(message, e); }
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfUnsupportedEncodingException(Throwable e) { super(e); }

	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfUnsupportedEncodingException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfUnsupportedEncodingException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfUnsupportedEncodingException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfUnsupportedEncodingException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.UnsupportedEncoding;
	}
}
