package com.intuit.spc.foundations.portability.security;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfFactory;


/**
 * Class to represent the realm of a user. The realm could be company, group etc.
 * 
 * @author barunachalam
 *
 */
public class SpcfRealm {

	/**
	 * Realm Id
	 */
	private String mRealmId;
	
	/**
	 * Realm specific private info - used to cache interal realm-id from oracle
	 * in a VPD deployment
	 */
	private long mInternalRealmId= 0;
	
	/**
	 * Default Constructor
	 */
	public SpcfRealm() {
		//Default constructor
	}

	/**
     * Create a new SpcfRealm instance with specified realm id.
     * @param realmId the Id of realm
     * @return new instance of SpcfRealm
     * @throws SpcfArgumentNullException - if specified realmId is null
     * @throws SpcfArgumentOutOfRangeException - if specified realmId is empty
	 */
	public static SpcfRealm createInstance(String realmId)
	{
		return SpcfFactory.getInstance().createRealm(realmId);
	}
	
	/**
	 * To construct realm with the given realm Id
	 * @param realmId Realm Id
	 */
	public SpcfRealm(String realmId) {
		mRealmId = realmId;
	}
	
	/**
	 * To get the realm Id
	 * @return Realm Id
	 */
	public String getRealmId()
	{
		return mRealmId;
	}
	
	/**
	 * Return the realm-specific information
	 * @return Object - object holding realm-specific information
	 */
	public long getInternalRealmId()
	{
		return mInternalRealmId;
	}
	
	/**
	 * Set the realm-specific information
	 * @param internalRealmId
	 */
	public void setInternalRealmId(long internalRealmId)
	{
		mInternalRealmId = internalRealmId;
	}
	
	/**
	 * Simply returns the realm Id of the realm.
	 * @return Realm Id
	 */
	@Override
	public String toString()
	{
		return mRealmId;
	}
	
	/**
	 * Return true or false depending on whether the passed in realm
	 * is equal to this realm.
	 * @param o Object to compare
	 * @return true or false depending on whether the passed in realm
	 * is equal to this realm.
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;

		if (o instanceof SpcfRealm)
		{
			SpcfRealm otherRealm = (SpcfRealm) o;
			
			//Just check the realm Id
			if(this.getRealmId().equals(otherRealm.getRealmId()))
				return true;
			else
				return false;
		}
		
		return false;
	}
	
	/**
	 * To get the hash code.
	 * @return Hash Code
	 */
	@Override
	public int hashCode()
	{
		return this.getRealmId().hashCode();
	}
}
