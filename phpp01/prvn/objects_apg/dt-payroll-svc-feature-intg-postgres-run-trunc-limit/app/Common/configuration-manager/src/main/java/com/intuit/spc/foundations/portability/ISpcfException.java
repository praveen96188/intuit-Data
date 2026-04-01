/* 
 * author		RGS                                                                    
 * department	SPC Foundations	                                            
 * project	    Portability                                                                   
 * 2004-06-21   Initial Implementation  
 */
package com.intuit.spc.foundations.portability;

/**
 * A portable interface for accessing exception members.
 *
 */
public interface ISpcfException extends ISpcfIncident
{
	/**
     *  Gets the Exception instance that caused the current exception.
	 */
	Throwable getInnerCause();

	
	/**
     *  Gets a string representation of the frames on the call stack 
	 *  at the time the current exception was thrown.
	 */
	String getStackTraceInfo();

	
	/**
     * Returns the associated Exception
	 * instance for this ISpcfException.
	 * If this ISpcfException instance is
	 * itself derived from Exception, the
	 * method should return the the instance itself.
	 *(specifically, this.
	 */
	Throwable getException();

	
	/**
	 * An identifier that indicates which Architecture defined
	 * this exception. 
	 */
	int getArchitectureId();
	

	/**
	 * An identifier that indicates which Module defined
	 * this exception. It should be unique to the architecture. 
	 */
	int getModuleId();
	

	/**
	 * An identifier that uniquely identifies this incident
	 * within the module. 
	 */
	int getErrorId();
	
	
	/**
	 * Returns an identifier that uniquely identifies this 
	 * exception instance.
	 */
	String getInstanceId();
}
