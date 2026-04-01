//$Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/portabilitySpecific/SpcfParamValidatorImpl.java#1 $
package com.intuit.spc.foundations.portabilitySpecific;

import com.intuit.spc.foundations.portability.*;

/**
 * Utility class for validating passed parameters. 
 *
 * @author ocardoso
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
public abstract class SpcfParamValidatorImpl extends SpcfParamValidator {

	 /**
     *  Validates that the given array is not empty or null. 
     *
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
	public static void checkIsNotNullOrEmptyArray(Object[] variable, String variableName)
	{
    	checkIsNotNull(variable, variableName);
		
		if (variable.length == 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName + " Length", "0");
		}
	}
    
    /**
     *  Validates that the given array is not empty or null. 
     *
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsNotNullOrEmptyArray(Object[] variable, String variableName,
    		int applicationId, int architectureId, int moduleId)
	{
    	checkIsNotNull(variable, variableName, applicationId, architectureId, moduleId);
		
		if (variable.length == 0)
		{
			throw new SpcfArgumentOutOfRangeException(
					variableName + " Length", "0", applicationId, architectureId, moduleId);
		}
	}
	
	 /**
     *  Validates that the given object is of or derived from the given type 
     *
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     * @param expectedType The type expected
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if variable is not assignable from 
     * the specified type or a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
	@SuppressWarnings("unchecked")
	public static void checkIsOfType(Object variable, String variableName, Class expectedType)
	{
		checkIsNotNull(variable, variableName);
		checkIsNotNull(expectedType, "expectedType");
		boolean assignableFrom = expectedType.isAssignableFrom(variable.getClass());
		if (!assignableFrom)
		{
			throw new SpcfArgumentOutOfRangeException(variableName + " Type", variable.getClass().toString());
		}
	}
	
	/**
     *  Validates that the given object is of or derived from the given type 
     *
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     * @param expectedType The type expected
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if variable is not assignable from 
     * the specified type or a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
	@SuppressWarnings("unchecked")
	public static void checkIsOfType(Object variable, String variableName, Class expectedType,
    		int applicationId, int architectureId, int moduleId)
	{
		checkIsNotNull(variable, variableName, applicationId, architectureId, moduleId);
		checkIsNotNull(expectedType, "expectedType", applicationId, architectureId, moduleId);
		boolean assignableFrom = expectedType.isAssignableFrom(variable.getClass());
		if (!assignableFrom)
		{
			throw new SpcfArgumentOutOfRangeException(variableName + " Type", variable.getClass().toString(),
					applicationId, architectureId, moduleId);
		}
	}
}
