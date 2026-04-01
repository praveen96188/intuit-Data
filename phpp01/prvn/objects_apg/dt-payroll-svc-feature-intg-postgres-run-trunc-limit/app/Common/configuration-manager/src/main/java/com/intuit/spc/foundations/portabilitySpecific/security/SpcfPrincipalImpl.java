package com.intuit.spc.foundations.portabilitySpecific.security;

import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.security.SpcfPrincipal;
import com.intuit.spc.foundations.portability.security.SpcfRealm;
import com.intuit.spc.foundations.portability.security.SpcfRole;

/**
 * A platform specific default implementation of SpcfPrincipal.
 */
public class SpcfPrincipalImpl extends SpcfPrincipal
{
	/**
	 * Global Identity Id
	 */
	private String mGlobalIdentityId;
	
	/**
	 * Current Realm
	 */
	private SpcfRealm mCurrentRealm;
	
	/**
	 * Effective Realms
	 */
	private SpcfList<SpcfRealm> mRealms;
	
	/**
	 * Name
	 */
	private String mName;
	
	/**
	 * Roles
	 */
	private SpcfList<SpcfRole> mRoles;

	/**
	 * Default constructor.
	 * @param id User Id
	 * @param realm Realm of the user
	 * @param name Name of the user
	 * @param roles Roles of the user
	 * @param effectiveRealms Effective Realms of the user
	 */
	public SpcfPrincipalImpl(String id, SpcfRealm realm, String name, SpcfList<SpcfRole> roles, SpcfList<SpcfRealm> effectiveRealms)
	{
		mGlobalIdentityId = id;
		mCurrentRealm = realm;
		mName = name;
		mRoles = roles;
		mRealms = effectiveRealms;
	}

	/**
	 * @see SpcfPrincipal#getId()
	 */
	@Override
	public String getId()
	{
		return mGlobalIdentityId;
	}

	/**
	 * @see SpcfPrincipal#getName()
	 */
	@Override
	public String getName()
	{
		return mName;
	}

	/**
	 * @see SpcfPrincipal#getCurrentRealm()
	 */
	@Override
	public SpcfRealm getCurrentRealm()
	{
		return mCurrentRealm;
	}

	/**
	 * @see SpcfPrincipal#setCurrentRealm(SpcfRealm)
	 */
	@Override
	public void setCurrentRealm(SpcfRealm currentRealm)
	{
		mCurrentRealm = currentRealm;
	}
	
	/**
	 * @see SpcfPrincipal#getRealms()
	 */
	@Override
	public SpcfList<SpcfRealm> getRealms()
	{
		return mRealms;
	}

	/**
	 * @see SpcfPrincipal#isAuthenticated()
	 */
	@Override
	public boolean isAuthenticated()
	{
		// the identity is authenticated, if identity's id is valid
		return mGlobalIdentityId!=null && mGlobalIdentityId.length() > 0;
	}

	/**
	 * @see SpcfPrincipal#getRoles()
	 */
	@Override
	public SpcfList<SpcfRole> getRoles()
	{
		return mRoles;
	}

	/**
	 * Override equal method to determine the equality of two principals.
	 * @param o object
	 * @return true if equal; false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;

		if (o instanceof SpcfPrincipalImpl)
		{
			SpcfPrincipalImpl oPrincipal = (SpcfPrincipalImpl)o;

			boolean idCheck = this.getId()!=null && oPrincipal.getId()!=null &&
				this.getId().equals(oPrincipal.getId());
			boolean realmIdCheck = this.getCurrentRealm().getRealmId()!=null && oPrincipal.getCurrentRealm().getRealmId()!=null &&
				this.getCurrentRealm().getRealmId().equals(oPrincipal.getCurrentRealm().getRealmId());
			boolean nameCheck = this.getName()!=null && oPrincipal.getName()!=null &&
				this.getName().equals(oPrincipal.getName());
			// don't need to check isAuthenticated value since it is based on id
			boolean rolesCheck = this.getRoles().equals(oPrincipal.getRoles());
			boolean realmsCheck = this.getRealms().equals(oPrincipal.getRealms());
			return idCheck && realmIdCheck && nameCheck && rolesCheck && realmsCheck;
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
		//Seed to start with.
		int result = 17;
		
		//Computing hash code as given in "Effective Java" book in java.sun.code
		result = 37 * result + this.getId().hashCode();
		
		result = 37 * result + this.getCurrentRealm().hashCode();
		
		result = 37 * result + this.getName().hashCode();
		
		result = 37 * result + this.getRoles().hashCode();
		
		result = 37 * result + this.getRealms().hashCode();
		
		return result;
	}

}
