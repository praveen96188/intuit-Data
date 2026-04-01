package com.intuit.spc.foundations.portability;

/**
 * Utility class for validating passed parameters. 
 *
 */
public abstract class SpcfParamValidator
{
    /**
     * Checks to ensure that the passed value is positive number
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if <code>number</code> is not
     * positive or a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsPositive(long variable, String variableName)
    {
    	checkIsNotNullOrEmptyString(variableName, "variableName");
		
		if (variable <= 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "" + variable);
		}
    }
    
    /**
     * Checks to ensure that the passed value is positive number
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
	 * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if <code>number</code> is not
     * positive or a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsPositive(long variable, String variableName, int applicationId, int architectureId, int moduleId)
    {
    	checkIsNotNullOrEmptyString(variableName, "variableName", applicationId, architectureId, moduleId);
		
		if (variable <= 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "" + variable, applicationId, architectureId, moduleId);
		}
    }

    /**
     * Checks to ensure that the passed value is a non-negative number. That is, the 
     * number must be greater than or equal to 0 or an exception will be thrown.
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if <code>number</code> is not
     * positive or a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsNonNegative(long variable, String variableName)
    {
    	checkIsNotNullOrEmptyString(variableName, "variableName");
		
		if (variable < 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "" + variable);
		}
    }
    
    /**
     * Checks to ensure that the passed value is a non-negative number. That is, the 
     * number must be greater than or equal to 0 or an exception will be thrown.
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if <code>number</code> is not
     * positive or a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsNonNegative(long variable, String variableName, int applicationId, int architectureId, int moduleId)
    {
    	checkIsNotNullOrEmptyString(variableName, "variableName", applicationId, architectureId, moduleId);
		
		if (variable < 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "" + variable, applicationId, architectureId, moduleId);
		}
    }
    
    /**
     * Checks to ensure that the passed value is not null
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsNotNull(Object variable, String variableName)
    {
    	if (variableName == null)
		{
			throw new SpcfArgumentNullException("variableName");
		}

		if (variableName.length() == 0)
		{
			throw new SpcfArgumentOutOfRangeException("variableName", "\"\"");
		}

		if (variable == null)
		{          
			throw new SpcfArgumentNullException(variableName);
		}
    }
    
    /**
     * Checks to ensure that the passed value is not null
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
    public static void checkIsNotNull(Object variable, String variableName, int applicationId, int architectureId, int moduleId)
    {
    	if (variableName == null)
		{
			throw new SpcfArgumentNullException("variableName", applicationId, architectureId, moduleId);
		}

		if (variableName.length() == 0)
		{
			throw new SpcfArgumentOutOfRangeException("variableName", "\"\"", applicationId, architectureId, moduleId);
		}

		if (variable == null)
		{          
			throw new SpcfArgumentNullException(variableName, applicationId, architectureId, moduleId);
		}
    }
    
    /**
     * Checks to ensure that the passed value is not null
     * @param variable Value to be checked. 
     * @param index integer used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * The exception message includes the parameter index for easy 
     * identification.
     */
    public static void checkIsNotNull(Object variable, int index)
    {
        if (variable == null)
		{          
			throw new SpcfArgumentNullException(""+ index);
		}
    }
    
    /**
     * Checks to ensure that the passed value is not null
     * @param variable Value to be checked. 
     * @param index integer used to identify variable in error message.
 	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if an argument is null
     * The exception message includes the parameter index for easy 
     * identification.
     */
    public static void checkIsNotNull(Object variable, int index, int applicationId, int architectureId, int moduleId)
    {
        if (variable == null)
		{          
			throw new SpcfArgumentNullException(""+ index, applicationId, architectureId, moduleId);
		}
    }

    /**
     * Checks to ensure that the passed string is not "" or null
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsNotNullOrEmptyString(String variable, String variableName)
    {
    	checkIsNotNull(variable, variableName);
		
		if (variable.length() == 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "\"\"");
		}
    }
    
    /**
     * Checks to ensure that the passed string is not "" or null
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
    public static void checkIsNotNullOrEmptyString(String variable, String variableName, int applicationId, int architectureId, int moduleId)
    {
    	checkIsNotNull(variable, variableName, applicationId, architectureId, moduleId);
		
		if (variable.length() == 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "\"\"", applicationId, architectureId, moduleId);
		}
    }
    
    /**
     * Checks to ensure that the passed string is not blank (contains 0 or more white spaces only) or null
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if a string argument is blank. 
     * The exception message includes the parameter name for easy identification.
     */
    public static void checkIsNotNullOrBlankString(String variable, String variableName)
    {
    	checkIsNotNull(variable, variableName);    	
    	
		if (variable.trim().length() == 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "\"\"");
		}
    }
    
    /**
     * Checks to ensure that the passed string is not blank (contains 0 or more white spaces only) or null
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
 	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if a string argument is blank. 
     * The exception message includes the parameter name for easy identification.
     */
    public static void checkIsNotNullOrBlankString(String variable, String variableName, int applicationId, int architectureId, int moduleId)
    {
    	checkIsNotNull(variable, variableName, applicationId, architectureId, moduleId);	
    	
		if (variable.trim().length() == 0)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "\"\"", applicationId, architectureId, moduleId);
		}
    }
    
   /**
    * Checks to ensure that the passesd array variable is not null or empty.
    * @param variable Array variable to be checked.
    * @param variableName Name used to identify variable in error message. 
    *  @throws SpcfArgumentNullException if variable is null
     * @throws SpcfIllegalArgumentException if array variable is empty.
    */
    public static void checkIsNotNullOrEmptyArray(Object[] variable, String variableName)
    {
        checkIsNotNull(variable, variableName);  
        if (variable.length == 0)
        {
            throw new SpcfIllegalArgumentException("Empty array- " + variableName);
        }
    }
    
    /**
     * Checks to ensure that the passesd array variable is not null or empty.
     * @param variable Array variable to be checked.
     * @param variableName Name used to identify variable in error message. 
  	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if variable is null
     * @throws SpcfIllegalArgumentException if array variable is empty.
     */
     public static void checkIsNotNullOrEmptyArray(Object[] variable, String variableName, int applicationId, int architectureId, int moduleId)
     {
         checkIsNotNull(variable, variableName, applicationId, architectureId, moduleId); 
         if (variable.length == 0)
         {
             throw new SpcfIllegalArgumentException("Empty array- " + variableName, applicationId, architectureId, moduleId);
         }
     }

    /**
     * Checks to ensure that the passed value is true
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if variable is false or 
     * a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsTrue(boolean variable, String variableName) 
    {
    	checkIsNotNullOrEmptyString(variableName, "variableName");
		
		if (variable != true)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "false");
		}
    }
    
    /**
     * Checks to ensure that the passed value is true
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
  	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if variable is false or 
     * a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsTrue(boolean variable, String variableName, int applicationId, int architectureId, int moduleId) 
    {
    	checkIsNotNullOrEmptyString(variableName, "variableName", applicationId, architectureId, moduleId);
		
		if (variable != true)
		{
			throw new SpcfArgumentOutOfRangeException(variableName, "false", applicationId, architectureId, moduleId);
		}
    }
    
    /**
     * Checks to ensure that the passed value is true
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     * @param message The message used in the generated exception if the test fails
     *
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if variable is false or 
     * a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsTrue(boolean variable, String variableName, String message) 
    {
        checkIsNotNullOrEmptyString(variableName, "variableName");
    	checkIsNotNullOrEmptyString(message, "message");
    	
        if (!variable) {
            throw new SpcfIllegalArgumentException(message);
        }
    }
    
    /**
     * Checks to ensure that the passed value is true
     * @param variable Value to be checked. 
     * @param variableName Name used to identify variable in error message.
     * @param message The message used in the generated exception if the test fails
  	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
     * @throws SpcfArgumentNullException if an argument is null
     * @throws SpcfArgumentOutOfRangeException if variable is false or 
     * a string argument is "". 
     * The exception message includes the parameter name for easy 
     * identification.
     */
    public static void checkIsTrue(boolean variable, String variableName, String message, int applicationId, int architectureId, int moduleId)  
    {
        checkIsNotNullOrEmptyString(variableName, "variableName", applicationId, architectureId, moduleId);
    	checkIsNotNullOrEmptyString(message, "message", applicationId, architectureId, moduleId);
    	
        if (!variable) {
            throw new SpcfIllegalArgumentException(message, applicationId, architectureId, moduleId);
        }
    }
    

    /**
     * Validates that the buffer, the offset, and the count are all valid.
     * This function should be used when copying into or out of array buffers.
     * For example, if you are reading a series of bytes into a buffer, the offset would refer
     * to the position in the buffer into which the copying will begin and count refers to the 
     * number of bytes that will be read into that buffer.
     * @param buffer the array to check 
     * @param offset the position within the buffer in which the operation (such as reading or writing) will begin
     * @param count the number of elements that will be affected by the operation
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if offset is less than 0 or count is less than 0  
	 * @throws SpcfIndexOutOfBoundsException if offset + count is greater than the buffer's length 
     */
    public static void checkArrayParams(byte[] buffer, int offset, int count)
    {
		// Make sure the array is not null: 
    	checkIsNotNull(buffer, "Buffer");
    	
    	// Offset cannot be negative. That is, it must be >= 0.
    	checkIsNonNegative((long)offset, "Offset");
    	
    	// Count cannot be negative. That is, it must be >= 0. 
    	checkIsNonNegative((long)count, "Count");
    	
    	// Offset + count must be less than the length of the array:
		if (offset + count > buffer.length) throw new SpcfIndexOutOfBoundsException("Offset + count cannot be greater than the size of the buffer.");
    }
    
    /**
     * Validates that the buffer, the offset, and the count are all valid.
     * This function should be used when copying into or out of array buffers.
     * For example, if you are reading a series of bytes into a buffer, the offset would refer
     * to the position in the buffer into which the copying will begin and count refers to the 
     * number of bytes that will be read into that buffer.
     * @param buffer the array to check 
     * @param offset the position within the buffer in which the operation (such as reading or writing) will begin
     * @param count the number of elements that will be affected by the operation
   	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if offset is less than 0 or count is less than 0  
	 * @throws SpcfIndexOutOfBoundsException if offset + count is greater than the buffer's length 
     */
    public static void checkArrayParams(byte[] buffer, int offset, int count, int applicationId, int architectureId, int moduleId)
    {
		// Make sure the array is not null: 
    	checkIsNotNull(buffer, "Buffer", applicationId, architectureId, moduleId);
    	
    	// Offset cannot be negative. That is, it must be >= 0.
    	checkIsNonNegative((long)offset, "Offset", applicationId, architectureId, moduleId);
    	
    	// Count cannot be negative. That is, it must be >= 0. 
    	checkIsNonNegative((long)count, "Count", applicationId, architectureId, moduleId);
    	
    	// Offset + count must be less than the length of the array:
		if (offset + count > buffer.length) throw new SpcfIndexOutOfBoundsException(
				"Offset + count cannot be greater than the size of the buffer.", applicationId, architectureId, moduleId);
    }
    
    /**
     * Validates that the buffer, the offset, and the count are all valid.
     * This function should be used when copying into or out of array buffers.
     * For example, if you are reading a series of bytes into a buffer, the offset would refer
     * to the position in the buffer into which the copying will begin and count refers to the 
     * number of bytes that will be read into that buffer.
     * @param buffer the array to check 
     * @param offset the position within the buffer in which the operation (such as reading or writing) will begin
     * @param count the number of elements that will be affected by the operation
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfIndexOutOfBoundsException if offset is less than 0, count is less than 0, or offset + count is greater than the buffer's length 
     */
    public static void checkArrayParams(char[] buffer, int offset, int count)
    {
    	try
    	{
    		// Make sure the array is not null: 
        	checkIsNotNull(buffer, "Buffer");
        	
        	// Offset cannot be negative. That is, it must be >= 0.
        	checkIsNonNegative((long)offset, "Offset");
        	
        	// Count cannot be negative. That is, it must be >= 0. 
        	checkIsNonNegative((long)count, "Count");
        	
        	// Offset + count must be less than the length of the array:
    		if (offset + count > buffer.length) throw new SpcfIndexOutOfBoundsException("Offset + count cannot be greater than the size of the buffer.");
    	}
    	catch (SpcfArgumentOutOfRangeException aoore)
    	{
    		throw new SpcfIndexOutOfBoundsException(aoore);
    	}
    }
    
    /**
     * Validates that the buffer, the offset, and the count are all valid.
     * This function should be used when copying into or out of array buffers.
     * For example, if you are reading a series of bytes into a buffer, the offset would refer
     * to the position in the buffer into which the copying will begin and count refers to the 
     * number of bytes that will be read into that buffer.
     * @param buffer the array to check 
     * @param offset the position within the buffer in which the operation (such as reading or writing) will begin
     * @param count the number of elements that will be affected by the operation
   	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 * 
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfIndexOutOfBoundsException if offset is less than 0, count is less than 0, or offset + count is greater than the buffer's length 
     */
    public static void checkArrayParams(char[] buffer, int offset, int count, int applicationId, int architectureId, int moduleId)
    {
    	try
    	{
    		// Make sure the array is not null: 
        	checkIsNotNull(buffer, "Buffer", applicationId, architectureId, moduleId);
        	
        	// Offset cannot be negative. That is, it must be >= 0.
        	checkIsNonNegative((long)offset, "Offset", applicationId, architectureId, moduleId);
        	
        	// Count cannot be negative. That is, it must be >= 0. 
        	checkIsNonNegative((long)count, "Count", applicationId, architectureId, moduleId);
        	
        	// Offset + count must be less than the length of the array:
    		if (offset + count > buffer.length) throw new SpcfIndexOutOfBoundsException(
    				"Offset + count cannot be greater than the size of the buffer.", applicationId, architectureId, moduleId);
    	}
    	catch (SpcfArgumentOutOfRangeException aoore)
    	{
    		throw new SpcfIndexOutOfBoundsException(aoore, applicationId, architectureId, moduleId);
    	}
    }
}
