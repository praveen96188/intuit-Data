/*
 * Created on Aug 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfPortabilityModuleException;

/**
 * @author ocardoso
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SpcfClassNotFoundException extends SpcfPortabilityModuleException 
{
	
	private static final long serialVersionUID = -937767639997583562L;
	private static String sMessage;
	
	static
	{
		sMessage = getDefaultMessage(SpcfPortabilityErrorEnum.ClassNotFound.getId());
	}

	/**
	 * Returns the message associated with this exception.
	 * @param assemblyName The name of the assembly.
	 * @param typeName The name of the type.
	 * @return A string message.
	 */
	public static String getErrorMessage(String assemblyName, String typeName)
	{
		return sMessage + typeName + " in " + assemblyName;
	}

	/**
	 * Class constructor
	 * @param library The library name.
	 * @param typeName The type name.
	 * @param e The exception that occurred.
	 */
	public SpcfClassNotFoundException(String library, String typeName, Exception e)
	{
		super(getErrorMessage(library, typeName), e);
	}

	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfClassNotFoundException(String message) 
	{
		super(message); 
	}
	
	/**
	 * Class constructor
	 * @param library The library name.
	 * @param typeName The type name.
	 * @param e The exception that occurred.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfClassNotFoundException(
			String library, String typeName, Exception e, int applicationId, int architectureId, int moduleId)
	{
		super(getErrorMessage(library, typeName), e, applicationId, architectureId, moduleId);
	}

	/**
	 * Class constructor
	 * @param message An exception message
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfClassNotFoundException(String message, int applicationId, int architectureId, int moduleId) 
	{
		super(message, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * We have to implement this method in order to uniquely identify this error within the
	 * module in a type safe manner.
	 * @return A SpcfPortabilityErrorEnum number.
	 */
	protected SpcfPortabilityErrorEnum getSpcfPortabilityError()
	{
		return SpcfPortabilityErrorEnum.ClassNotFound;
	}
}
