package com.intuit.spc.foundations.primary.config;

/**
 * The root exception used by the configuration providers to indicate errors that
 * may occur while called by the CMS.  
 *
 */

public abstract class SpcfConfigProviderException extends SpcfConfigModuleException
{
	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = -2034231844249238927L;

	/**
     * Constructs an instance with a detail error message
     * @param errorMsg The detailed error message
     */
    public SpcfConfigProviderException(String errorMsg)
    {
        super(errorMsg);
    }
    
    /**
     * Constructs an instance with a detail error message and cause.
     * @param errorMsg The detailed error message
     * @param cause The error that caused this exception
     */
    public SpcfConfigProviderException(String errorMsg, Throwable cause)
    {
        super(errorMsg, cause);
    }
}
