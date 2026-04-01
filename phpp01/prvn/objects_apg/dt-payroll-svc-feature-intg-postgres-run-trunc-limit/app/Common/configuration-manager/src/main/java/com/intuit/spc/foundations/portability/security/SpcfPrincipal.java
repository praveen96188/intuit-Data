package com.intuit.spc.foundations.portability.security;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.collections.SpcfList;

/**
 * Portable representation of a principal.  SpcfPrincipal contains
 * the identity and its related authentication and authorization informations.
 * SpcfPrincipal shall be created after authentication and set it to the
 * current thread so that it can be used in authorization.
 * <p>
 * To create a new instance of SpcfPrincipal
 * <pre>
 * SpcfPrincipal.createInstance(
 *       "I123456",  // unique identityId or authId
 *       new SpcfRealm("R123456"),  // unique realmId
 *       "JohnDoe", // the name of identity
 *       SpcfArrayList.&lt;SpcfRole&gt;createInstance(), // the list of roles
 *       SpcfArrayList.&lt;SpcfRealm&gt;createInstance()); // the list of realms
 * </pre>
 * </p>
 * <p>
 * SpcfPrincipal also provides methods to store and retrieve the principal of
 * current thread.
 * </p>
 * <p>
 * To store and retrieve the current principal
 * <pre>
 *   SpcfPrincipal.setCurrent(bhpPrincipal);
 *   ...
 *   currentPrincipal = SpcfPrincipal.getCurrent();
 * </pre>
 * </p>
 * <p>
 * SpcfPrincipal utilizes
 * SpcfPrincipal class is configured with a SpcfCurrentPrincipalStrategy object,
 * maintains a reference to a SpcfCurrentPrincipalStrategy object and
 * defines an interface that lets SpcfCurrentPrincipalStrategy access its data.
 * </p>
 * <p>
 * To customize how to retreive the current principal
 * <pre>
 *   SpcfPrincipal.setCurrentPrincipalStrategy(bhpCurrentPrincipalStrategy);
 * </pre>
 * </p>
 */
public abstract class SpcfPrincipal
{
	/**
	 * Gets the identity ID which is globally unique.
	 * It can be used to represent AuthID of SPC-Auth.
	 * @return the unique ID of identity
	 */
	public abstract String getId();

	/**
	 * Gets the current realm of the user.
	 * The realm has the realm Id which can be used to represent RealmId of SPC-Auth.
	 * @return Current Realm
	 */
	public abstract SpcfRealm getCurrentRealm();

	/**
	 * Sets the current realm of the user.
	 * The realm has the realm Id which can be used to represent RealmId of SPC-Auth.
	 * @param currentRealm Current Realm
	 */
	public abstract void setCurrentRealm(SpcfRealm currentRealm);
	
	/**
	 * Gets the list of effective realms.
	 * It can be used to represent the list of authorized RealmId of SPC-Auth.
	 * @return List of effective unique realms.
	 */
	public abstract SpcfList<SpcfRealm> getRealms();
	
	/**
	 * Gets the name of the principal.
	 * It can be used to represent the name of user or group.
	 * @return the name of the principal.
	 */
	public abstract String getName();

	/**
	 * Check whether the identity is authenticated or not.
	 * @return true if authenticated, false otherwise.
	 */
	public abstract boolean isAuthenticated();

	/**
	 * Gets the roles associated with the principal.
	 * @return the list of role associated with the principal.
	 */
	public abstract SpcfList<SpcfRole> getRoles();


	/**
	 * Creates an instance of SpcfPrincipal with specified values.
	 * @param id the unique id of identity
	 * @param realm the current realm of the user
	 * @param name the name of principal
	 * @param roles the roles associated with principal
	 * @param effectiveRealms List of effective realms of the user
	 * @return a new instance of SpcfPrincipal
	 */
	public static SpcfPrincipal createInstance(String id, SpcfRealm realm, String name, SpcfList<SpcfRole> roles, SpcfList<SpcfRealm> effectiveRealms)
	{
		return SpcfFactory.getInstance().createPrincipal(id,realm,name,roles,effectiveRealms);
	}

	/**
	 * Strategy instance to manage current principal
	 */
	private volatile static SpcfCurrentPrincipalStrategy sStrategyInstance = null;

	/**
	 * To synchronize creation of singleton instance object
	 */
	private static final String mutex = new String("SpcfCurrentPrincipalStrategy-Mutex");

	/**
	 * Use this method instead of using sStrategyInstance directly.
	 * @return the instance of SpcfCurrentPrincipalStrategy
	 */
	private static SpcfCurrentPrincipalStrategy getStrategyInstance()
	{
		if(sStrategyInstance == null)
		{
			synchronized (mutex)
			{
				if(sStrategyInstance == null)
				{
					sStrategyInstance = SpcfFactory.getInstance().createCurrentPrincipalStrategy();
				}
			}
		}
		return sStrategyInstance;
	}

	/**
	 * Stores the principal of the current thread. The specified
	 * principal can be null.  This method delegates the actual
	 * task to <code> setCurrentPrincipal </code> method in the
	 * strategy class.
	 * @param principal the target principal to set; can be null
	 */
	public static void setCurrent(SpcfPrincipal principal)
	{
		getStrategyInstance().setCurrentPrincipal(principal);
	}

	/**
	 * Retrieves the principal of the current thread. This method
	 * delegates the actual task to <code> getCurrentPrincipal </code>
	 * method in the strategy class.
	 * @return the current principal; null if the principal
	 * cannot be retrieved.
	 */
	public static SpcfPrincipal getCurrent()
	{
		return getStrategyInstance().getCurrentPrincipal();
	}

	/**
	 * Set the strategy to customize the way to manage the current principal. Note
	 * that this will overwrite the existing strategy.
	 * @param s current strategy instance
	 */
	public static void setCurrentPrincipalStrategy(SpcfCurrentPrincipalStrategy s)
	{
		synchronized(mutex)
		{
			sStrategyInstance = s;
		}
	}


}
