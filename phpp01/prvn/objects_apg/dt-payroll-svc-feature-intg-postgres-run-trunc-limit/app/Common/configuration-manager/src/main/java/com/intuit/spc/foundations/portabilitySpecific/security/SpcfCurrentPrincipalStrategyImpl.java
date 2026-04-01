package com.intuit.spc.foundations.portabilitySpecific.security;

import com.intuit.spc.foundations.portability.security.SpcfCurrentPrincipalStrategy;
import com.intuit.spc.foundations.portability.security.SpcfPrincipal;

/**
 * A platform specific implementation of SpcfCurrentPrincipalStrategy.
 * <p>
 * Default implementaion utilizes InheritableThreadLocal for Java side and
 * System.Threading.Thread.CurrentPrincipal for .NET side.
 * </p>
 * <p>
 * Note that both InheritableThreadLocal and Thread.CurrentPrincipal
 * allows to propagate the current principal to new thread created.
 * </p>
 */
public class SpcfCurrentPrincipalStrategyImpl extends SpcfCurrentPrincipalStrategy
{
	// this static variable is thread unique, don't need to sync
	private static InheritableThreadLocal<SpcfPrincipal>
		sCurrentPrincipal = new InheritableThreadLocal<SpcfPrincipal>();

	/**
	 * @see SpcfCurrentPrincipalStrategy#getCurrentPrincipal()
	 */
	@Override
	public SpcfPrincipal getCurrentPrincipal()
	{
		return sCurrentPrincipal.get();
	}

	/**
	 * @see SpcfCurrentPrincipalStrategy#setCurrentPrincipal(SpcfPrincipal)
	 */
	@Override
	public void setCurrentPrincipal(SpcfPrincipal principal)
	{
		// allow null
		sCurrentPrincipal.set(principal);
	}
}
