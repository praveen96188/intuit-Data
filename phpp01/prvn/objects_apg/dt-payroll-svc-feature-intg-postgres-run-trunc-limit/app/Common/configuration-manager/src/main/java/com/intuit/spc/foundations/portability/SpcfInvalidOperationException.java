/* 
 * author		Manoj Garg                                                                   
 * department	SPC Foundations	                                            
 * project	    Portability                                                                   
 * 2005-11-04   Initial Implementation  
 */
package com.intuit.spc.foundations.portability;

/**
 * The exception that is thrown when a method call is invalid for the object's 
 * current state.
 * SpcfInvalidOperationException is used in cases when the failure to invoke a 
 * method is caused by reasons other than invalid arguments. For example, 
 * InvalidOperationException is thrown by:  
 * If pop() or peek() methods are called on empty SpcfStack object.
*/
public class SpcfInvalidOperationException extends SpcfPortabilityModuleException
{
	private static final long serialVersionUID = -1665323515580831033L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.InvalidOperation.getId());
	}
	/**
	 * Class constructor
	 */
	public SpcfInvalidOperationException() { super(sMessage); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfInvalidOperationException(String message) { super(message); }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfInvalidOperationException(String message, Throwable e) { super(message, e); }
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfInvalidOperationException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfInvalidOperationException(int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidOperationException(String message, int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidOperationException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
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
	public SpcfInvalidOperationException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
	/**
    * We have to implement this method in order to uniquely identify
	* this error within the module in a type safe manner
	*/
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.InvalidOperation;
	}
}
