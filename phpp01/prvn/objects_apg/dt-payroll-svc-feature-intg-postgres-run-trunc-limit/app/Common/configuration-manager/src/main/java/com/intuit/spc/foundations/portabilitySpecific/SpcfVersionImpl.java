package com.intuit.spc.foundations.portabilitySpecific;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfVersion;

/**
 * SpcfVersion class provides a formal representation for versions, including
 * SPCF portable language version and platform version.
 * @author gwang
 */
public class SpcfVersionImpl extends SpcfVersion 
{   
	private String major;
	private String minor;
	private String release;
	private String patch;
	
	/**
	 * Default constructor.
	 *
	 */
	public SpcfVersionImpl() 
	{
		major = null;
		minor = null;
		release = null;
		patch = null;
	} 
	
	/**
	 * Constructor from string.
	 * @param ver input version string.
	 * @throws SpcfIllegalArgumentException if input version string doesn't
	 * start with "x.y." where x/y is the required major/minor number.
	 */
	public SpcfVersionImpl(String ver) 
	{
		String[] result = ver.split("\\.");
		if (result.length >= 2) 
		{
			major = result[0];
			minor = result[1];
		} else 
		{
			throw new SpcfIllegalArgumentException(
					"ver is not an valid version string.");
		}
		
		if (result.length >= 3) 
		{
			release = result[2];
		}
		if (result.length == 4) 
		{
			patch = result[3];
		}	    	    
	}
	
	/**
	 * Constructor from numbers.
	 * 
	 * @param maj  the major number.
	 * @param min the minor number.
	 * @param rel the release number.
	 * @param pat the patch number.
	 */
	public SpcfVersionImpl(int maj, int min, int rel, int pat) 
	{
		SpcfParamValidator.checkIsNotNull(maj, "maj");
		SpcfParamValidator.checkIsNotNull(min, "min");
		SpcfParamValidator.checkIsNotNull(rel, "rel");
		SpcfParamValidator.checkIsNotNull(pat, "pat");
		
		major = "" + maj;
		minor = "" + min;
		release = "" + rel;
		patch = "" + pat; 
	}
	
    /**
     * Return the major string.
     * @return the major string.
     */
    public String getMajor()
    {
    	return major;
    }
 
    /**
     * Return the minor string.
     * @return the minor string.
     */    
    public String getMinor() 
    {
    	return minor;
    }
    
    /**
     * Return the major and minor string.
     * @return the major and minor string.
     */
    public String getMajorAndMinor() 
    {
    	return major + "." + minor;
    }
    
    /**
     * Return the release number.
     * @return the release number.
     */
    public String getRelease() 
    {
    	return release;
    }
    
    /**
     * Return the patch number.
     * @return the patch number.
     */
    public String getPatch() 
    {
    	return patch;
    }
    
    /**
     * Return the whole string containing up to four parts: major, minor, 
     * release and patch numbers.
     * @return the whole string.
     */
    public String toString() 
    {
    	String version = "";
    	if (major != null) 
    	{
    		version = version + major;    	
	    	if (minor != null) 
	    	{
	    		version = version + "." + minor;
	    	} else 
	    	{
	    		return version;
	    	}	    	
	    	
	    	if (release != null) 
	    	{
	    		version = version + "." + release;
	    	} else 
	    	{
	    		return version;
	    	}
	    	if (patch != null) 
	    	{
	    		version = version + "." + patch;
	    	} else 
	    	{
	    		return version;
	    	} 
    	}		    
    	return version;
    }
}
    

