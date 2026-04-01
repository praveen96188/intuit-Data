package com.intuit.spc.foundations.portability;

import com.intuit.spc.foundations.portability.annotations.SpcfNonPortableClass;

/**
 * The base class of all portable checked exceptions. 
 * If you are writing portable code that needs to create 
 * its own exceptions, derive from either 
 * SpcfException class (for java checked exceptions) or  
 * SpcfRuntimeException class (for java unchecked exceptions). 
 * SpcfException extends java.lang.Exception 
 * and adds a method to return the stack trace as
 * a string. 
 */
@SpcfNonPortableClass
public class SpcfException extends Exception implements ISpcfException 
{

	private static final long serialVersionUID = -3473787189812516280L;

    private static final String DefaultGuid = "12345678-8765-4321-1234-567887654321";
    
	/**
	 * Constructs a new exception instance
	 */
	public SpcfException() 
	{
		super();
        setGuid();
	}
	
	/**
	 * Constructs an exception with a message
	 */
	public SpcfException(String message) 
	{
		super(message);
        setGuid();
	}
	
	/**
	 * Constructs an exception with a message and chained exception
	 */
	public SpcfException(String message, Throwable e) 
	{
		super(message, e);
        setGuid();
	}
	
	/**
	 * Constructs an exception with a chained exception
	 */
	public SpcfException(Throwable e) 
	{
		super(e);
        setGuid();
	}
	
	/**
	 * Constructs a new exception instance
	 */
	public SpcfException(int applicationId, int architectureId, int moduleId) 
	{
		super();
        setGuid();
        setExceptionCode(applicationId, architectureId, moduleId);
	}
	
	/**
	 * Constructs an exception with a message
	 */
	public SpcfException(String message, int applicationId, int architectureId, int moduleId) 
	{
		super(message);
        setGuid();
        setExceptionCode(applicationId, architectureId, moduleId);
	}
	
	/**
	 * Constructs an exception with a message and chained exception
	 */
	public SpcfException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
	{
		super(message, e);
        setGuid();
        setExceptionCode(applicationId, architectureId, moduleId);
	}
	
	/**
	 * Constructs an exception with a chained exception
	 */
	public SpcfException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{
		super(e);
        setGuid();
        setExceptionCode(applicationId, architectureId, moduleId);
	}
	
	/**
	 * Sets overrides for application ID, architecture ID, and module Id. IDs equal to
	 * zero will not be overridden.
	 */
	protected void setExceptionCode(int applicationId, int architectureId, int moduleId)
	{
		if (applicationId != 0)
		{
			this.overrideApplicationId(applicationId);
		}
		
		if (architectureId != 0)
		{
			this.overrideArchitectureId(architectureId);
		}
		
		if (moduleId != 0)
		{
			this.overrideModuleId(moduleId);
		}
	}
	
	/**
	 * Provided so that handlers can change the id
	 */
	protected int mApplicationIdOverride = 0;
	
	/**
	 * Indicates if id was overriden
	 */
	protected boolean mApplicationIdOverridden = false;
	
	/**
	 * True if the id has been overridden
	 * @return
	 */
	protected boolean getApplicationIdOverridden()
	{
		return mApplicationIdOverridden;
	}

	/**
	 * Used to override the id associated with the exception
	 * @param id The new id
	 */
	public void overrideApplicationId(int id)
	{
		mApplicationIdOverridden = true;
		mApplicationIdOverride = id;
	}
	
	/**
	 * An identifier that indicates which Application is claiming
	 * responsibility for this exception
	 */
	public int getApplicationId()
	{
		if (mApplicationIdOverridden)
		{
			return mApplicationIdOverride;
		}
		else
		{
			return getApplicationIdForDefiningType();
		}
	}
	
	/**
	 * An identifier that indicates which Application defined
	 * this exception.  
	 * @return
	 */
	protected int getApplicationIdForDefiningType()
	{
		return 0;
	}
	
	
	/**
	 * Provided so that handlers can change the id
	 */
	protected int mArchitectureIdOverride = 0;
	
	/**
	 * Indicates if id was overriden
	 */
	protected boolean mArchitectureIdOverridden = false;
	
	/**
	 * True if the id has been overridden
	 * @return
	 */
	protected boolean getArchitectureIdOverridden()
	{
		return mArchitectureIdOverridden;
	}

	/**
	 * Used to override the id associated with the exception
	 * @param id The new id
	 */
	public void overrideArchitectureId(int id)
	{
		mArchitectureIdOverridden = true;
		mArchitectureIdOverride = id;
	}
	
	/**
	 * An identifier that indicates which Architecture is claiming
	 * responsibility for this exception
	 */
	public int getArchitectureId()
	{
		if (mArchitectureIdOverridden)
		{
			return mArchitectureIdOverride;
		}
		else
		{
			return getArchitectureIdForDefiningType();
		}
	}
	
	/**
	 * An identifier that indicates which Architecture defined
	 * this exception.  
	 * @return
	 */
	protected int getArchitectureIdForDefiningType()
	{
		return 0;
	}

	/**
	 * Provided so that handlers can change the id
	 */
	protected int mModuleIdOverride = 0;
	
	/**
	 * Indicates if id was overriden
	 */
	protected boolean mModuleIdOverridden = false;
	
	/**
	 * True if the id has been overridden
	 * @return
	 */
	protected boolean getModuleIdOverridden()
	{
		return mModuleIdOverridden;
	}

	/**
	 * Used to override the id associated with the exception
	 * @param id The new id
	 */
	public void overrideModuleId(int id)
	{
		mModuleIdOverridden = true;
		mModuleIdOverride = id;
	}
	
	/**
	 * An identifier that indicates which Module is claiming
	 * responsibility for this exception
	 */
	public int getModuleId()
	{
		if (mModuleIdOverridden)
		{
			return mModuleIdOverride;
		}
		else
		{
			return getModuleIdForDefiningType();
		}
	}
	
	/**
	 * An identifier that indicates which Module defined
	 * this exception.  
	 * @return
	 */
	protected int getModuleIdForDefiningType()
	{
		return 0;
	}

	/**
	 * Provided so that handlers can change the id
	 */
	protected int mErrorIdOverride = 0;
	
	/**
	 * Indicates if id was overriden
	 */
	protected boolean mErrorIdOverridden = false;
	
	/**
	 * True if the id has been overridden
	 * @return
	 */
	protected boolean getErrorIdOverridden()
	{
		return mErrorIdOverridden;
	}

	/**
	 * Used to override the id associated with the exception
	 * @param id The new id
	 */
	public void overrideErrorId(int id)
	{
		mErrorIdOverridden = true;
		mErrorIdOverride = id;
	}
	
	/**
	 * An identifier that indicates which Error is claiming
	 * responsibility for this exception
	 */
	public int getErrorId()
	{
		if (mErrorIdOverridden)
		{
			return mErrorIdOverride;
		}
		else
		{
			return getErrorIdForDefiningType();
		}
	}
	
	/**
	 * An identifier that indicates which Error defined
	 * this exception.  
	 * @return
	 */
	protected int getErrorIdForDefiningType()
	{
		return 0;
	}

	/**
	 * Returns cause of this exception
	 */
	public Throwable getInnerCause()
	{
		return this.getCause();
	}
	
	/**
	 * Returns the most wrapped cause of this exception
	 */
	public Throwable getInnerMostCause()
	{
		Throwable ex = this;

        while (ex.getCause() != null)
        {
              ex = ex.getCause(); 
        }

        return ex;
	}

	/**
	 *  Returns stack trace associated with this exception
	 */
	public String getStackTraceInfo()
	{
		return SpcfPortabilityUtilNT.getStackTraceInfo(this);
	}

	/**
	 *  Returns this as a Throwable
	 */
	public Throwable getException()
	{
		return this;
	}

	/**
	 * Provided so that handlers can change the message; .Net doesn't
	 * allow you to change an Exception's message
	 */
	protected String mMessageOverride = null;
	
	/**
	 * Indicates if message was overriden
	 */
	protected boolean mMessageOverridden = false;
	
	/**
	 * True if the message has been overridden
	 * @return
	 */
	protected boolean getMessageOverridden()
	{
		return mMessageOverridden;
	}

	/**
	 * Used to override the message associated with the exception
	 * @param message The new message
	 */
	public void overrideMessage(String message)
	{
		mMessageOverridden = true;
		mMessageOverride = message;
	}
	
	/**
	 * The message associated with this exception
	 */
	public String getMessage()
	{
		if (mMessageOverridden == false)
		{
			return super.getMessage();
		}
		else
		{
			return mMessageOverride;
		}
	}

	/**
	 * The incident level of this incident.. you're code probably 
	 * isn't smart enough to really know that something should be "Error" 
	 * vs "Critical" for the life of your app, so you probably shouldn't 
	 * worry about setting this. The default is Error.
	 */
	protected SpcfPriority mPriority = SpcfPriority.Error;

	/**
	 * The level of this incident.. The default is Error.
	 */
	public SpcfPriority getPriority()
	{
		return mPriority;
	}
	
	/**
	 * The level of this incident.. your code probably 
	 * isn't smart enough to really know that something should be "Error" 
	 * vs "Critical" for the life of your app, so you probably shouldn't 
	 * worry about setting this. The default is Error.
	 */
	public void setPriority(SpcfPriority val)
	{
		mPriority = val;
	}

	/**
	 * Used to override the calculated operational id
	 */
	protected String mOperationalIdOverride = null;
	
	/**
	 * Indicates if id was overriden
	 */
	protected boolean mOperationalIdOverridden = false;
	
	/**
	 * True if the id has been overridden
	 * @return
	 */
	protected boolean getOperationalIdOverridden()
	{
		return mOperationalIdOverridden;
	}

	/**
	 * Used to override the id associated with the exception
	 * @param id The new id
	 */
	public void overrideOperationalId(String id)
	{
		mOperationalIdOverridden = true;
		mOperationalIdOverride = id;
	}
	
	/**
	 * Implementation of ISpcfIncident.getOperationalId()
	 * that uses SpcfIncident.calculateOperationalId().
	 * @see com.intuit.spc.foundations.portability.ISpcfIncident#getOperationalId()
	 * @see com.intuit.spc.foundations.portability.SpcfIncident#calculateOperationalId(int, int, int)
	 */
	public String getOperationalId()
	{
		if (mOperationalIdOverridden == false)
		{
			return SpcfIncident.calculateOperationalId(getArchitectureId(), 
					getModuleId(), getErrorId());
		}
		else
		{
			return mOperationalIdOverride;
		}
	}
	
	
	/**
	 * The GUID that is generated upon construction.
	 */
	protected String mInstanceId = DefaultGuid;
	
    /**
     * Tries to set the Instance Id for this exception to a system generated
     * GUID value.  If the SpcfSystem is not available, then a default GUID
     * value is used.
     */
    private void setGuid()
    {
        try
        {
        	mInstanceId = SpcfUniqueId.generateRandomUniqueIdString(); 
        } catch (Throwable e) {
            mInstanceId = DefaultGuid;
        }
    }
	
	/**
	 * Returns an identifier that uniquely identifies this exception instance.
	 */
	public String getInstanceId()
	{
		return mInstanceId;
	}
	
	/**
	 * Returns a string that identifies this exception instance by application id,
	 * architecture id, module id, and error id. A string is returned in the form 
	 * appID.archID.moduleID.errID
	 */
	public String getExceptionCode()
	{
		String errorCode = 	this.getApplicationId() + "." + this.getArchitectureId() + "." + 
							this.getModuleId() + "." + this.getErrorId();
		
		return errorCode;
	}
}