package com.intuit.spc.foundations.portabilitySpecific.net;

import java.net.MalformedURLException;
import java.net.URL;

import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.net.SpcfMalformedUrlException;
import com.intuit.spc.foundations.portability.net.SpcfUrl;

/**
 * The implementation for the abstract SpcfURL class.
 */
public class SpcfUrlImpl extends SpcfUrl
{
	/**
	 * The encapsulated third party runtime URL implementation
	 */
	private URL mPlatformSpecific;
	
	/**
	 * Default constructor. <br>
	 * This constructor is provided to support Xml serialization. When this constructor is used, 
	 * setSpec(String) must be called to initialize the object with a valid url specification.
	 */
	public SpcfUrlImpl()
	{	
		// left intentially blank
	}
	
	/**
	 * Gets the URL specification of the current object. <br/>
	 * This proerty is provided to support Xml serialization.
	 * @return url specification
	 */
	public String getSpec()
	{
		return toString();
	}
	
	/**
	 * Sets the URL specification of the current object. <br/>
	 * This proerty is provided to support Xml serialization.
	 * @param spec URL specification for the URL 
	 * @throws SpcfMalformedUrlException if the given url specification is invalid.
	 */
	public void setSpec(String spec)
	{	
		try
		{
			mPlatformSpecific = new URL(spec);
		}
		catch(MalformedURLException ex)
		{
			throw new SpcfMalformedUrlException(ex);
		}
	}
	
    /**
     * A public constructor.
     * @param spec The spec to be used for the given URL.
     */
	public SpcfUrlImpl(String spec)
	{	
		SpcfParamValidator.checkIsNotNull(spec, "spec");	
		try
		{
			mPlatformSpecific = new URL(spec);	
			if(!isProtocolSupported(mPlatformSpecific.getProtocol()))
			{
				throw new SpcfMalformedUrlException(mPlatformSpecific.getProtocol() + " is not supported.");
			}
		}
		catch(MalformedURLException ex)
		{	
			throw new SpcfMalformedUrlException(ex);
		}
	}	
	
    /**
     * A public constructor.
     * @param protocol The protocol to be used by the URL.
     * @param host The host to be used by the URL.
     * @param file The file to be used by the URL.
     */
	public SpcfUrlImpl(String protocol, String host, String file)	
	{	
		this(protocol, host, -1, file);
	}
	
     /**
     * A public constructor.
     * @param protocol The protocol to be used by the URL.
     * @param host The host to be used by the URL.
     * @param port The port to be used by the URL.
     * @param file The file to be used by the URL.
     */
	public SpcfUrlImpl(String protocol, String host, int port, String file)
	{	
		try
		{
			SpcfParamValidator.checkIsNotNullOrBlankString(protocol, "protocol");
			SpcfParamValidator.checkIsNotNullOrBlankString(host, "host");
			SpcfParamValidator.checkIsNotNull(file, "file");
		}
		catch(SpcfArgumentOutOfRangeException ex)
		{
			throw new SpcfMalformedUrlException();
		}
		
		if(!isProtocolSupported(protocol))
		{
			throw new SpcfMalformedUrlException(protocol + " is not supported.");
		}
		try
		{
			mPlatformSpecific = new URL(protocol, host, port, file);
		}
		catch(MalformedURLException ex)
		{	
			throw new SpcfMalformedUrlException(ex);
		}
	}	
	
	/**
	 * The supported protocols are "http", "https", "ftp".
	 * @param protocol a string protocol
	 * @return true if the protocol is supported, false if not
	 */
	private boolean isProtocolSupported(String protocol)
	{
		boolean ret = false;
		if(protocol != null && 
				(protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https") || protocol.equalsIgnoreCase("ftp")))
		{
			ret = true;
		}
		return ret;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getQuery()
	 */
	@Override
	public String getQuery()
	{
		assertInit();
		String query = null;
		//following changes are necessary to bring same semantics between Java and .NET
		if(!mPlatformSpecific.getProtocol().equalsIgnoreCase("ftp"))
		{
			query =  mPlatformSpecific.getQuery();
		}
		return query;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getPath()
	 */
	@Override
	public String getPath() 
	{
		assertInit();
		String path = mPlatformSpecific.getPath();		                   
		if (path != null && path.length() == 1 && path.equalsIgnoreCase("/"))
	    {
	        path = "";
	    } 
		return path;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getUserInfo()
	 */
	@Override
	public String getUserInfo() 
	{
		assertInit();
		return mPlatformSpecific.getUserInfo();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getAuthority()
	 */
	@Override
	public String getAuthority() 
	{
		assertInit();
		String authority = mPlatformSpecific.getAuthority();
		//following changes are necessary to bring same semantics between Java and .NET
		if(mPlatformSpecific.getUserInfo() != null)
		{
			//remove user info from the authority string returned by Java
			int idx = authority.indexOf('@');
			authority = authority.substring(idx+1);
		}
		
		if(mPlatformSpecific.getPort() == mPlatformSpecific.getDefaultPort())
		{
			//remove the port number if it is equal to the default port number
			String port = String.valueOf(mPlatformSpecific.getPort());
			authority = authority.replaceFirst(":"+port, "");
		}
		else if(mPlatformSpecific.getPort() == -1)
		{	
//			remove the port number if it is equal to the default port number			
			authority = authority.replaceFirst(":-1", "");
		}
		return authority;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getPort()
	 */
	@Override
	public int getPort() 
	{
		assertInit();
		int port = mPlatformSpecific.getPort();
		if(port == -1)
		{
			port = mPlatformSpecific.getDefaultPort();
		}
		return port;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getDefaultPort()
	 */
	@Override
	public int getDefaultPort() 
	{
		assertInit();
		return mPlatformSpecific.getDefaultPort();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getProtocol()
	 */
	@Override
	public String getProtocol()
	{
		assertInit();
		return mPlatformSpecific.getProtocol();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getHost()
	 */
	@Override
	public String getHost()
	{
		assertInit();
		return mPlatformSpecific.getHost();	
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getFile()
	 */
	@Override
	public String getFile() 
	{
		assertInit();
		String file = mPlatformSpecific.getFile();		                   
		if (file != null && file.equals("/"))
	    {
			file = "";
	    } 
		return file;	
	}

	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrl#getRef()
	 */
	@Override
	public String getRef() 
	{
		assertInit();
		return mPlatformSpecific.getRef();	
	}	
	
    /**
     * Returns the platform-specific implementation object for this class.
     * @return platform-specific implementation object
     */
	public URL toSpecific() 
	{
		return mPlatformSpecific;	
	}

	/**
	 * Get the string representation of this URL object.
	 * @return the string representation of this URL object.
	 */
	@Override
    public String toString()
    {
    	return mPlatformSpecific.toExternalForm();
	}
    
	/**
	 * helper method to throw SpcfIllegalStateException if the encapsulated URL member variable
	 * has not been initialized.
	 * @throws SpcfIllegalStateException if mPlatformSpecific is null
	 */
    private void assertInit()
    {	
    	if(mPlatformSpecific == null)
    	{
    		throw new SpcfIllegalStateException();
    	}
    }
}
