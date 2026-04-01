package com.intuit.spc.foundations.portability;

import com.intuit.spc.foundations.portability.io.*;

/**
 * SpcfSystem is the system class that provides useful methods
 * to determine the state of the current system and to access
 * system resources
 */
public abstract class SpcfSystem
{
	
	/**
     * file instance that is used for static methods
     */
    protected static SpcfSystem sSystem; //instance for static methods

    /**
     * initialization method called before static methods can be used
     */
    protected static void initialize()
	{
    	if(sSystem == null)
    	{
    		sSystem = SpcfFactory.getInstance().createSystem();
    	}
    }

    /**
     * Gets the newline string defined for this environment. 
     */ 
    public static String getNewLine()
	{
    	initialize();
    	return sSystem.doGetNewLine();
    }
 
    /**
     * Instance implementation called by getNewLine. 
     */ 
    protected abstract String doGetNewLine();

    /**
     * Gets the name of the person logged on to the system. 
     */  
    public static String getUserName()
	{
    	initialize();
    	return sSystem.doGetUserName();
    }

    /**
     * Instance implementation called by getUserName. 
     */ 
    protected abstract String doGetUserName();
  
 
    /**
     * Gets the string representation of the file separator for the system. 
     */     
    public static String getFileSeparator()
	{
    	initialize();
    	return sSystem.doGetFileSeparator();
    }

    /**
     * Instance implementation called by getFileSeparator. 
     */ 
    protected abstract String doGetFileSeparator();
 
    /**
     * Gets the string representation of the path separator for the system. 
     */     
    public static String getPathSeparator()
	{
    	initialize();
    	return sSystem.doGetPathSeparator();
    }

    /**
     * Instance implementation called by getPathSeparator. 
     */ 
    protected abstract String doGetPathSeparator();
 
    
    /**
     * Gets the fully qualified path of the current directory. 
     */     
    public static String getCurrentDirectory()
	{
    	initialize();
    	return sSystem.doGetCurrentDirectory();
    }  
   
    /**
     * Instance implementation called by getCurrentDirectory. 
     */     
    protected abstract String doGetCurrentDirectory();

    /**
     * Terminates this process and gives the underlying operating system the specified exit code.
     * The argument serves as a status code; by convention, 
     * a nonzero status code indicates abnormal termination.
     * @param exitCode the exit status code 
     */
    public static void exit(int exitCode)
	{
    	initialize();
    	sSystem.doExit(exitCode);
    }
	
    /**
     * Instance implementation called by exit. 
     */     
    protected abstract void doExit(int exitCode);
    
    /**
     * Runs the garbage collector. 
     * 
     * Calling this method suggests that the 
     * Java or .Net runtime expends effort 
     * toward recycling unused objects 
     * in order to make the memory they 
     * currently occupy available for quick reuse. 
     * When control returns from the method call, 
     * the Java or .Net runtime has made a best effort 
     * to reclaim space from all discarded objects. 
     */
    public static void gc()
	{
    	initialize();
    	sSystem.doGC();
    }
	
    /**
     * Instance implementation called by gc. 
     */     
    protected abstract void doGC();
   
    /**
     * Retrieves the total number of bytes currently 
     * thought to be allocated. 
     */
    public static long getMemoryAllocated()
	{
      	initialize();
    	return sSystem.doGetMemoryAllocated();
    }

    /**
     * Instance implementation called by getMemoryAllocated. 
     */     
    protected abstract long doGetMemoryAllocated();
    
    /**
     * Reads a line from the "standard" input (i.e. Console). 
     * Typically the "standard" input corresponds to keyboard input or 
     * another input source specified by the host environment or user.
     *  
     * @return line from console 
     * @throws SpcfIOException if an I/O error occurs. 
     */
    public static String readConsoleLine() throws SpcfIOException
    {
        initialize();
        return sSystem.doReadConsoleLine();
    }
    
    /**
     * For internal use only - called by readConsoleLine()
     * @return
     * @throws SpcfIOException
     */
    protected abstract String doReadConsoleLine() throws SpcfIOException;
 
    /**
     * Returns true if called from Java runtime and false if called from .NET.
     * @return true, only if called from Java runtime.
     */
    public static boolean isJavaPlatform() 
    {
        initialize();
        return sSystem.doIsJavaPlatform();
    }
    
    /**
     * The abstract non-portable implementation for isJavaPlatform
     * @return True if the platform is Java, false otherwise.
     */
    protected abstract boolean doIsJavaPlatform() ;
 
    /**
     * 	Write a string to the standard Error.
     * @param strError String to write out to standard Error
     */
	public static void systemWriteError(String strError) 
	{
		initialize();
		sSystem.doSystemWriteError(strError);
	}

    /**
     * NOT FOR PUBLIC USAGE - internally called by SystemWriteError()
     * @param strError The string to write.
     */
	protected abstract void doSystemWriteError(String strError) ;

    /**
     * 	Write a string to the standard Out.
     * @param strOut The string to write out to standard Out
     */
	public static void systemWriteOut(String strOut) 
	{
		initialize();
		sSystem.doSystemWriteOut(strOut);
	}

    /**
     * NOT FOR PUBLIC USAGE - internally called by SystemWriteOut()
     * @param strOut The string to write.
     */
	protected abstract void doSystemWriteOut(String strOut) ;

	
	/**
	 * Gets the string path to the requested common directory.<br/><br/>
	 * 
	 * Because the location of this common directory is highly platform-specific and because
	 * not all platforms will support every type of common directory, this may throw an
	 * SpcfNotImplementedException. 
	 *   
	 * @return directory as a string
	 * @throws SpcfNotImplementedException if the requested common directory is invalid for the current platform.
	 */
	protected abstract String doGetCommonDirectory(SpcfCommonDirectoryEnum commonDirectory);

	
	/**
	 * Gets the string path to the requested common directory.<br/><br/>
	 * 
	 * Because the location of this common directory is highly platform-specific and because
	 * not all platforms will support every type of common directory, this may throw an
	 * SpcfNotImplementedException. 
	 *   
	 * @return directory as a string
	 * @throws SpcfNotImplementedException if the requested common directory is invalid for the current platform.
	 */
	public static String getCommonDirectory(SpcfCommonDirectoryEnum commonDirectory)
	{
    	initialize();
    	return sSystem.doGetCommonDirectory(commonDirectory);
	}

	/**
	 * Returns a string describing the architecture for the operating system
	 * on which the application is running.
	 * @return The operating system architecture information.
	 */
    public static String getOSArchitecture()
	{
    	initialize();
    	return sSystem.doGetOSArchitecture();
    }
    
	/**
	 * Returns a string describing the architecture for the operating system
	 * on which the application is running.
	 * @return The operating system architecture information.
	 */
    protected abstract String doGetOSArchitecture();

    /**
     * Returns a string containing the name of the operating system on which
     * the application is running.
     * @return The name of the operating system.
     */
    public static String getOSName()
	{
    	initialize();
    	return sSystem.doGetOSName();
    }
    
    /**
     * Returns a string containing the name of the operating system on which
     * the application is running.
     * @return The name of the operating system.
     */
    protected abstract String doGetOSName();
	
	/**
	 * Returns a string describing the version for the operating system
	 * on which the application is running.
	 * @return The operating system version information.
	 */
    public static String getOSVersion()
	{
    	initialize();
    	return sSystem.doGetOSVersion();
    }
    
	/**
	 * Returns a string describing the version for the operating system
	 * on which the application is running.
	 * @return The operating system version information.
	 */
    protected abstract String doGetOSVersion();

	/**
	 * To get process ID of the current process. 
	 * @return Process ID
	 */
	public static String getCurrentProcessID()
	{
		//TODO: QA to test this API across all platforms
		initialize();
		return sSystem.doGetCurrentProcessID();
	}	
	
	/**
	 * To get process ID of the current process.
	 * @return Process ID
	 */
	protected abstract String doGetCurrentProcessID();
	
	/**
	 * Return the platform version.
	 * @return A SpcfVersion object representing the platform version.
	 */
	public static SpcfVersion getPlatformVersion() 
	{
		initialize();
    	return sSystem.doGetPlatformVersion();
	}
	
	/**
	 * Return the platform version.
	 * @return A SpcfVersion object representing the platform version.
	 */
	public abstract SpcfVersion doGetPlatformVersion();
	
	/**
	 * Return the platform version to run as.
	 * 
	 * <p>
	 * For Java, it will simply return {@link #getPlatformVersion()} unless it is set explicitly
	 * through {@link #setRunningAsPlatformVersion(SpcfVersion)} or {@link #setRunningAsPlatformVersionString(String)}.<br>
	 * For .Net, it will return the default version which is "2.0" unless it is set explicitly
	 * through {@link #setRunningAsPlatformVersion(SpcfVersion)} or {@link #setRunningAsPlatformVersionString(String)}.<br>
	 * </p>
	 * 
	 * @return A SpcfVersion object representing the platform version to run as.
	 */
	public static SpcfVersion getRunningAsPlatformVersion() 
	{
		initialize();
    	return sSystem.doGetRunningAsPlatformVersion(); 
	}
	
	/**
	 * Return the platform version to run as.
	 * 
	 * <p>
	 * For Java, it will simply return string representation of {@link #getPlatformVersion()} unless it is set explicitly
	 * through {@link #setRunningAsPlatformVersion(SpcfVersion)} or {@link #setRunningAsPlatformVersionString(String)}.<br>
	 * For .Net, it will return the default version which is "2.0" unless it is set explicitly
	 * through {@link #setRunningAsPlatformVersion(SpcfVersion)} or {@link #setRunningAsPlatformVersionString(String)}.<br>
	 * </p>
	 * @return A string representing the platform version to run as.
	 */
	public static String getRunningAsPlatformVersionString() 
	{
		initialize();
    	return sSystem.doGetRunningAsPlatformVersionString(); 
	}
	
	/**
	 * Set the platform version to run as.
	 * 
	 * <p>
	 * For .Net, the only possible values can be set are 2.0 and 3.0. And you can set 3.0 only if the system has .Net 3.0 installed. 
	 * So take caution before setting the Running As Platform Version. It is highly recommeneded NOT to set it explicitly. By default, 2.0 will be
	 * used as the Running As Platform Version.
	 * </p>
	 * @param sv the version to run as.
	 */
	public static void setRunningAsPlatformVersion(SpcfVersion sv)
	{
		initialize();
    	sSystem.doSetRunningAsPlatformVersion(sv); 
	}		
	
	/**
	 * Set the platform version to run as.
	 *  	 
	 * <p>
	 * For .Net, the only possible values can be set are 2.0 and 3.0. And you can set 3.0 only if the system has .Net 3.0 installed. 
	 * So take caution before setting the Running As Platform Version. It is highly recommeneded NOT to set it explicitly. By default, 2.0 will be
	 * used as the Running As Platform Version.
	 * </p>
	 * @param sv the version to run as.
	 */
	public static void setRunningAsPlatformVersionString(String sv)
	{
		initialize();
    	sSystem.doSetRunningAsPlatformVersionString(sv); 
	}		

	/**
	 * Return the platform version to run as.
	 * @return A SpcfVersion object representing the platform version to run as.
	 */
	public abstract SpcfVersion doGetRunningAsPlatformVersion();
	
	/**
	 * Return the platform version to run as.
	 * @return A SpcfVersion object representing the platform version to run as.
	 */
	public abstract String doGetRunningAsPlatformVersionString();
	
	/**
	 * Set the platform version to run as.
	 * @param sv the version to run as.
	 */
	public abstract void doSetRunningAsPlatformVersion(SpcfVersion sv);
	
	/**
	 * Set the platform version to run as.
	 * @param sv the version to run as.
	 */
	public abstract void doSetRunningAsPlatformVersionString(String sv);
	

	/**
	 * Redirect the console error output  
	 */
	public static void redirectConsoleError()
	{
		initialize();
    	sSystem.doRedirectConsoleError();
	} 
	
	/**
	 * Set back the console error output to default
	 */
	public static void setBackConsoleError()
	{
		initialize();
    	sSystem.doSetBackConsoleError();
	}
	
	/**
	 * Redirect the console output  
	 */
	public static void redirectConsoleOut()
	{
		initialize();
    	sSystem.doRedirectConsoleOut();
	}
	/**
	 * Set back the console output to default
	 */
	public static void setBackConsoleOut()
	{
		initialize();
    	sSystem.doSetBackConsoleOut();
	} 
	
	/**
	 * Redirect the console error output  
	 */
	protected abstract void doRedirectConsoleError(); 
	
	/**
	 * Set back the console error output to default
	 */
	protected abstract void doSetBackConsoleError();
	
	/**
	 * Redirect the console output  
	 */
	protected abstract void doRedirectConsoleOut(); 
	
	/**
	 * Set back the console output to default
	 */
	protected abstract void doSetBackConsoleOut();
    
    /**
     * On Java, does nothing except return the original object. On .NET, casts the incoming object to object
     * and returns the result. This gets around compiler warnings on the Java side about unnecessary casts that
     * are actually necessary because when code is translated to C#, the C# compiler insists on seeing the cast.
     * This method is temporarily added to this class pending it being added to portability, at which point this
     * method will be deprecated and then removed. We could also remove warnings of this kind by upgrading to
     * Eclipse 3.3 and adding SuppressWarnings("cast"), but that upgrade isn't planned for a while.
     * @param obj Object to cast on .NET or to pretend to cast on Java.
     * @return Object cast to object on .NET and completely unmodified on Java.
     */
	@Deprecated
    public static final Object castToObject(Object obj)
    {
        initialize();
        return sSystem.doCastToObject(obj);
    }
    
    /**
     * On Java, does nothing except return the original object. On .NET, casts the incoming object to object
     * and returns the result. This gets around compiler warnings on the Java side about unnecessary casts that
     * are actually necessary because when code is translated to C#, the C# compiler insists on seeing the cast.
     * This method is temporarily added to this class pending it being added to portability, at which point this
     * method will be deprecated and then removed. We could also remove warnings of this kind by upgrading to
     * Eclipse 3.3 and adding SuppressWarnings("cast"), but that upgrade isn't planned for a while.
     * @param obj Object to cast on .NET or to pretend to cast on Java.
     * @return Object cast to object on .NET and completely unmodified on Java.
     */
	@Deprecated
    protected abstract Object doCastToObject(Object obj);
}
