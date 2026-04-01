package com.intuit.spc.foundations.portabilitySpecific;

import com.intuit.spc.foundations.portability.SpcfCommonDirectoryEnum;
import com.intuit.spc.foundations.portability.SpcfSystem;
import com.intuit.spc.foundations.portability.SpcfVersion;
import com.intuit.spc.foundations.portability.io.SpcfIOException;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfDirectoryImpl;

import java.io.*;
import java.lang.management.ManagementFactory;

/**
* A platform specific implementation of SpcfSystem
*/
public class SpcfSystemImpl extends SpcfSystem
{
	protected static final String CurrentDirProperty = "user.dir";
	protected static final String FileSeparatorProperty = "file.separator";
	protected static final String NewlineProperty = "line.separator";
	protected static final String PathSeparatorProperty = "path.separator";
	protected static final String UserNameProperty = "user.name";

	protected static final String OsArchProperty = "os.arch";
	protected static final String OsNameProperty = "os.name";
	protected static final String OsVersionProperty = "os.version";
	
	//this is the runtime object needed to determine memory allocated
	protected static Runtime sRuntime = Runtime.getRuntime();
	
	protected static SpcfVersion runningAsPlatformVersion = 
		SpcfSystem.getPlatformVersion();
	
	
	private static PrintStream tmpErr = null;
    private static PrintStream ps2Err = null; 
    private static PrintStream tmpOut = null;
    private static PrintStream ps2Out = null; 
    
	/**
	 * Return the platform version.
	 * @return A SpcfVersion object representing the platform version.
	 */
	public SpcfVersion doGetPlatformVersion() 
	{				
		String ver = System.getProperty("java.version");
		//System.out.println("jvm version = " +  ver);
		String newVer = ver.replace("_", ".");
		//System.out.println("new jvm version = " +  newVer);
		return new SpcfVersionImpl(newVer); 
	}
	
	/**
	 * Return the platform version to run as.
	 * @return A SpcfVersion object representing the platform version to run as.
	 */
	public SpcfVersion doGetRunningAsPlatformVersion()
	{
		return runningAsPlatformVersion;
	}
	
	/**
	 * Return the platform version to run as.
	 * @return A String representing the platform version to run as.
	 */
	public String doGetRunningAsPlatformVersionString()
	{
		if (runningAsPlatformVersion != null) 
		{
			return runningAsPlatformVersion.toString();
		} else 
		{
			return null;
		}
	}
	
	/**
	 * Set the platform version to run as.
	 * 
	 */
	public void doSetRunningAsPlatformVersion(SpcfVersion sv) 
	{
		runningAsPlatformVersion = sv;
	}	
	
	/**
	 * Set the platform version to run as.
	 * 
	 */
	public void doSetRunningAsPlatformVersionString(String sv) 
	{
		runningAsPlatformVersion = new SpcfVersionImpl(sv);
	}	

	
    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#getNewLine()
     */	
    protected String doGetNewLine()
	{
    	return System.getProperty(NewlineProperty);
    }
   
    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#getUserName()
     */	
    protected String doGetUserName()
	{
    	return System.getProperty(UserNameProperty);
    }
 
    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#getFileSeparator()
     */	
    protected String doGetFileSeparator()
	{
    	return System.getProperty(FileSeparatorProperty);
    }

    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#getPathSeparator()
     */	
    protected String doGetPathSeparator()
	{
    	return System.getProperty(PathSeparatorProperty);
    }

 
    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#getCurrentDirectory()
     */	    
    protected String doGetCurrentDirectory()
	{
    	return System.getProperty(CurrentDirProperty);
    }


    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#doExit(int)
     */	
    protected void doExit(int exitCode)
	{
    	System.exit(exitCode);
    }
	
    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#getMemoryAllocated()
     */	
    protected long doGetMemoryAllocated()
	{
	    return sRuntime.totalMemory () - sRuntime.freeMemory ();
    }

    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#doGC()
     */	
    protected void doGC()
	{
    	System.gc();
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.SpcfSystem#doReadConsoleLine()
	 */
	protected String doReadConsoleLine() throws SpcfIOException
	{
	    String line = "";
		BufferedReader console = new
        	BufferedReader( new
        	    InputStreamReader(System.in));
		
		try
        {
            line = console.readLine();
        }
        catch(IOException e)
        {
            throw new SpcfIOException(e);
        }
        
        return line;
	}

    protected boolean doIsJavaPlatform()
    {
    	return true;
    }

	protected void doSystemWriteError(String strError)
	{
		System.err.println(strError);
	}

	protected void doSystemWriteOut(String strOut)
	{
		System.out.println(strOut);
	}

	
    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#doGetCommonDirectory(SpcfCommonDirectoryEnum)
     */	
	@Override
	protected String doGetCommonDirectory(SpcfCommonDirectoryEnum commonDirectory)
	{
		// Get the OS name:
		String osName = System.getProperty("os.name").toLowerCase();
		
		// This is used for convenience. Uncomment what you need:
		boolean isWindows = osName.startsWith("windows");
		boolean isOldWindows = (isWindows && osName.indexOf("95") > 0);
		/*
		boolean isMac = osName.startsWith("mac");
		boolean isUnix = (osName.startsWith("sunos") 
				|| osName.startsWith("solaris")
				|| osName.startsWith("mac os x")
				|| osName.startsWith("os/400")
				|| osName.startsWith("hp-ux")
				|| osName.startsWith("aix")
				|| osName.startsWith("linux")
				|| osName.startsWith("irix")
				|| osName.startsWith("digital unix"));
		boolean isLinux = osName.startsWith("linux"); 
		boolean isOS2 = osName.startsWith("os/2");
		*/ 

		// This is the path to the file that we will return:
		String path = "";
		
		// Assemble the path based on java system properties and os name:
		if (commonDirectory == SpcfCommonDirectoryEnum.UserApplicationData)
		{
			path = System.getProperty("user.home");
			if (!path.endsWith(File.separator)) path += File.separator;
			path += (isWindows && !isOldWindows ? "Application Data" : "AppData");
		}
		else if (commonDirectory == SpcfCommonDirectoryEnum.AllUsersApplicationData)
		{
			if (isWindows && !isOldWindows) 
			{
				path = System.getProperty("user.home");
				if (!path.endsWith(File.separator)) path += File.separator;
				path += ".." + File.separator + "All Users" + File.separator + "Application Data";
			}
			else 
			{ 
				path = System.getProperty("user.home");
				if (!path.endsWith(File.separator)) path += File.separator;
				path += ".." + File.separator + "AppData";
			}
		}
		else if (commonDirectory == SpcfCommonDirectoryEnum.Temporary)
		{
			path = System.getProperty("java.io.tmpdir");
		}
		else if (commonDirectory == SpcfCommonDirectoryEnum.UserDocuments)
		{
			path = System.getProperty("user.home");
		}
			
		return new SpcfDirectoryImpl(path).getPath();
	}
	
	
	protected String doGetOSArchitecture()
	{
    	return System.getProperty(OsArchProperty);
    }

    protected String doGetOSName()
	{
    	return System.getProperty(OsNameProperty);
    }
    
    protected String doGetOSVersion()
	{
    	return System.getProperty(OsVersionProperty);
    }

	/*
	 * @see SpcfSystem#getCurrentProcessID()
	 */
	@Override
	protected String doGetCurrentProcessID()
	{
		String returnValue = ManagementFactory.getRuntimeMXBean().getName();
		if(returnValue != null && returnValue.indexOf("@") != -1)			
		{
			return returnValue.substring(0, returnValue.indexOf("@"));
		}
		return "";
	}
	
	/**
	 * @see SpcfSystem#redirectConsoleError()
	 */ 
	protected void doRedirectConsoleError()
	{
		//save the standard error output.
        tmpErr = System.err;
        
        // Redirect the console error output.
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ps2Err = new PrintStream(bs);
        System.setErr(ps2Err);
	}
	
	/**
	 * @see SpcfSystem#setBackConsoleError()
	 */ 
	protected void doSetBackConsoleError()
	{  
        if (tmpErr != null)
        {
            System.setErr(tmpErr);
        }
        if (ps2Err != null)
        {
        	ps2Err.close();
        }
	}
	
	/**
	 * @see SpcfSystem#redirectConsoleOut()
	 */ 
	protected void doRedirectConsoleOut()
	{
		//save the standard console output.
        tmpOut = System.out;
        
        // Redirect the console output.
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ps2Out = new PrintStream(bs);
        System.setOut(ps2Out);
	}
	
	/**
	 * @see SpcfSystem#setBackConsoleOut()
	 */ 
	protected void doSetBackConsoleOut()
	{
		if (tmpOut != null)
        {
            System.setOut(tmpOut);
        }
        if (ps2Out != null)
        {
        	ps2Out.close();
        }
	}

    /**
     * @see com.intuit.spc.foundations.portability.SpcfSystem#doCastToObject(java.lang.Object)
     */
    @Override
    @Deprecated
    protected Object doCastToObject(Object obj)
    {
        return obj;
    }
}


