package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
* Portable exception class.
* InstantiationException
*/
public class SpcfInstantiationException extends SpcfPortabilityModuleException {
	

	private static final long serialVersionUID = 8367516769617768058L;

	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ClassCreateInstance.getId());
	}
	
    /**
     * Default constructor. 
     */
    public SpcfInstantiationException()
    {
        super(sMessage);
    }
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfInstantiationException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param message the exception message.
	 */
	public SpcfInstantiationException(String message) 
	{ 
		 super(message);
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfInstantiationException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * We have to implement this method in order to uniquely identify this error within the
	 * module in a type safe manner.
	 * @return A SpcfPortabilityErrorEnum number.
	 */
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.Instantiation;
	}
}
