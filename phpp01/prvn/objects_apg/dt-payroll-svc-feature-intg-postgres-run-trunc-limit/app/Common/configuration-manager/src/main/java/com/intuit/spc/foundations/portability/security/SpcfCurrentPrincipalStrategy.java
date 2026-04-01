package com.intuit.spc.foundations.portability.security;

import com.intuit.spc.foundations.portability.SpcfFactory;

/**
 * SpcfCurrentPrincipalStrategy class declares an interface common
 * to all supported strategies. SpcfPrincipal uses this interface
 * to call the strategy defined by actual strategy implementation.
 * <p>
 * SPC-F provides a default strategy which utilizes InheritableThreadLocal
 * in java and System.Threading.Thread.CurrentPrincipal in .NET.
 * Create custom strategy class extending SpcfCurrentPrincipalStrategy
 * to customize the way to store/retrieve the principal of current thread.
 */
public abstract class SpcfCurrentPrincipalStrategy {

	/**
	 * Defines how to set the current principal.
	 * @param principal the target principal to set
	 */
	public abstract void setCurrentPrincipal(SpcfPrincipal principal);

	/**
	 * Defines how to retrieve the current principal.
	 * @return the current principal, null if current principal
	 * cannot be retrieved
	 */
	public abstract SpcfPrincipal getCurrentPrincipal();

	/**
	 * Create a new instance of SpcfCurrentPrincipalStrategy
	 * @return the current principal strategy
	 */
	public static SpcfCurrentPrincipalStrategy createInstance()
	{
		return SpcfFactory.getInstance().createCurrentPrincipalStrategy();
	}
}
