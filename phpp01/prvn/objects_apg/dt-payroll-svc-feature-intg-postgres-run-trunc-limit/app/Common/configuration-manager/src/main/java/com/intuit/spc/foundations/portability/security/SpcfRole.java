package com.intuit.spc.foundations.portability.security;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfFactory;

/**
 * Portable representation of a role.  The concept of role is 
 * abstracted into a new type, SpcfRole, for changeability.  
 * Currently, it is only required to return the name of the role
 * in string-based representation. SpcfPrincipal contains the 
 * list of role associated with the principal.
 * <pre>
 *   SpcfRole role = SpcfRole.createInstance("Admin");
 * </pre>
 * <p>
 * This class is expected to change in furture to return
 * the list of permission associated with specific offering.
 * </p>
 * @see com.intuit.spc.foundations.portability.security.SpcfPrincipal
 */
public abstract class SpcfRole
{
	/**
	 * Gets the name of the role
	 * @return the role's name
	 */
	public abstract String getName();
	
	/**
	 * Gets the name of the role
	 * @return the role's name
	 */
	public abstract String getRoleType();
	
	/**
     * Create a new SpcfRole instance with specified name.
     * @param name the name of role
     * @return new instance of SpcfRole
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	public static SpcfRole createInstance(String name)
	{
		return SpcfFactory.getInstance().createRole(name);
	}
	
	/**
     * Create a new SpcfRole instance with specified name or type.
     * @param name the name of role
     * @param roleType the type of role
     * @return new instance of SpcfRole
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	public static SpcfRole createInstance(String name, String roleType)
	{
		return SpcfFactory.getInstance().createRole(name, roleType);
	}
}
