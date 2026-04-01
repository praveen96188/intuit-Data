package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
* Portable exception class.
* IllegalAccessException 
*/
public class SpcfIllegalAccessException extends SpcfPortabilityModuleException {
	
	private static final long serialVersionUID = -8696144002163991069L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.IllegalAccess.getId());
	}

	/**
	 * Returns the message associated with this exception.
	 * @param assemblyName The name of the assembly.
	 * @param typeName The name of the type.
	 * @return A string message.
	 */
	private static String getErrorMessage(String assemblyName, String typeName){
		return sMessage + " " + typeName + " in " + assemblyName;
	}
	
	/**
	 * Class constructor
	 * @param assemblyName The name of the assembly.
	 * @param typeName The name of the type.
	 */
	public SpcfIllegalAccessException(String assemblyName, String typeName) 
	{ 
		super(getErrorMessage(assemblyName, typeName)); 
	}
	
	/**
	 * Class constructor
	 * @param assemblyName The name of the assembly.
	 * @param typeName The name of the type.
	 * @param e A throwable exception
	 */
	public SpcfIllegalAccessException(String assemblyName, String typeName, Throwable e)
	{ 
		super(getErrorMessage(assemblyName, typeName), e); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfIllegalAccessException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param assemblyName The name of the assembly.
	 * @param typeName The name of the type.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalAccessException(
			String assemblyName, String typeName, int applicationId, int architectureId, int moduleId) 
	{ 
		super(getErrorMessage(assemblyName, typeName), applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param assemblyName The name of the assembly.
	 * @param typeName The name of the type.
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalAccessException(
			String assemblyName, String typeName, Throwable e, int applicationId, int architectureId, int moduleId)
	{ 
		super(getErrorMessage(assemblyName, typeName), e, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfIllegalAccessException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	

	/**
	 * We have to implement this method in order to uniquely identify this error within the
	 * module in a type safe manner.
	 * @return A SpcfPortabilityErrorEnum number.
	 */
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError(){
		return SpcfPortabilityErrorEnum.IllegalAccess;
	}
}
