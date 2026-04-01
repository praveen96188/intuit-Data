package com.intuit.spc.foundations.portabilitySpecific.security;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.security.SpcfRole;

/**
 * A platform specific implementation of SpcfRole.
 */
public class SpcfRoleImpl extends SpcfRole
{
	/**
	 * Role Name
	 */
	private String mName;
	
	/**
	 * Role Type
	 */
	private String mRoleType;
	
	/**
	 * Private Constructor
	 */
	private SpcfRoleImpl()
	{
		//Private Constructor
	}
	
	/**
     * Constructor to create a new SpcfRole instance with specified name
     * @param name the name of role
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	public SpcfRoleImpl(String name)
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(name, "name");		
		mName = name;
		mRoleType = "";
	}
	
	/**
     * Constructor to create a new SpcfRole instance with specified name
     * @param name the name of role
     * @param roleType the type of role
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	public SpcfRoleImpl(String name, String roleType)
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(name, "name");
		SpcfParamValidator.checkIsNotNullOrBlankString(roleType, "roleType");	
		mName = name;
		mRoleType = roleType;
	}

	/**
	 * @see SpcfRole#getName()
	 */
	@Override
	public String getName()
	{
		return mName;
	}
	
	/**
	 * @see SpcfRole#getRoleType()
	 */
	@Override
	public String getRoleType() 
	{
		return mRoleType;
	} 	
	
	/**
	 * Override equal method to determine the equality of two roles.
	 * @param o object
	 * @return true if equal; false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		
		if (o instanceof SpcfRoleImpl)
		{
			SpcfRoleImpl oRole = (SpcfRoleImpl)o;
		
			boolean nameCheck = 
				this.mName != null && oRole.mName!= null &&
				this.mName.equals(oRole.mName);
			
			boolean roleTypeCheck = 
				this.mRoleType != null && oRole.mRoleType != null &&
				this.mRoleType.equals(oRole.mRoleType);
			
			return nameCheck && roleTypeCheck;
		}
		
		return false;
	}
	
	/***
	 * Overridden since the equals operator is also overridden.
	 * @return Returns the hashcode for this instance.
	 */
	@Override
	public int hashCode()
	{
		// Seed to start with.
		int result = 17;
		
		//Computing hash code as given in "Effective Java" book in java.sun.code
		result = 37 * result + this.mName.hashCode();
		
		result = 37 * result + this.mRoleType.hashCode();
		
		return result;
	}
}
