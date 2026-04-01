package com.intuit.spc.foundations.portability;

import com.intuit.spc.foundations.portability.annotations.SpcfNonPortableClass;
import com.intuit.spc.foundations.portability.annotations.serialization.SpcfXmlBinderExcludeElement;
/**
 * The base class of all portable non-checked exceptions. 
 * If you are writing portable code that needs to create 
 * its own exceptions, derive from either 
 * SpcfException class (for java checked exceptions) or  
 * SpcfRuntimeException class (for java unchecked exceptions). 
 * SpcfRuntimeException extends java.lang.RuntimeException 
 * and adds a method to return the stack trace as
 * a string. 
 * 
 */
@SpcfNonPortableClass
public class SpcfRuntimeException extends RuntimeException implements ISpcfException 
{

	private static final long serialVersionUID = 7870119247434442487L;

    private static final String DefaultGuid = "12345678-8765-4321-1234-567887654321";
    
	/**
	 * Constructs a new exception instance
	 */
	public SpcfRuntimeException() 
	{
		super();
        setGuid();
	}
	
	/**
	 * Constructs an exception with a message
	 */
	public SpcfRuntimeException(String message) 
	{
		super(message);
        setGuid();
	}
	
	/**
	 * Constructs an exception with a message and chained exception
	 */
	public SpcfRuntimeException(String message, Throwable e) 
	{
		super(message, e);
        setGuid();
	}
	
	/**
	 * Constructs an exception with a chained exception
	 */
	public SpcfRuntimeException(Throwable e) 
	{
		super(e);
        setGuid();
	}
	
	/**
	 * Constructs a new exception instance
	 */
	public SpcfRuntimeException(int applicationId, int architectureId, int moduleId) 
	{
		super();
        setGuid();
        setExceptionCode(applicationId, architectureId, moduleId);
	}
	
	/**
	 * Constructs an exception with a message
	 */
	public SpcfRuntimeException(String message, int applicationId, int architectureId, int moduleId) 
	{
		super(message);
        setGuid();
        setExceptionCode(applicationId, architectureId, moduleId);
	}
	
	/**
	 * Constructs an exception with a message and chained exception
	 */
	public SpcfRuntimeException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
	{
		super(message, e);
        setGuid();
        setExceptionCode(applicationId, architectureId, moduleId);
	}
	
	/**
	 * Constructs an exception with a chained exception
	 */
	public SpcfRuntimeException(Throwable e, int applicationId, int architectureId, int moduleId) 
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

    /**
     * Provides programmatic access to the stack trace information printed by
     * printStackTrace(). Returns an array of stack trace elements, each
     * representing one stack frame. The zeroth element of the array (assuming the
     * array's length is non-zero) represents the top of the stack, which is the
     * last method invocation in the sequence. Typically, this is the point at
     * which this throwable was created and thrown. The last element of the array
     * (assuming the array's length is non-zero) represents the bottom of the stack,
     * which is the first method invocation in the sequence. Some virtual machines
     * may, under some circumstances, omit one or more stack frames from the stack
     * trace. In the extreme case, a virtual machine that has no stack trace
     * information concerning this throwable is permitted to return a zero-length
     * array from this method. Generally speaking, the array returned by this
     * method will contain one element for every frame that would be printed by
     * printStackTrace.
     * 
     * @return an array of stack trace elements representing the stack trace
     * pertaining to this throwable.
     */
    @SpcfXmlBinderExcludeElement
    @Override
    public StackTraceElement[] getStackTrace()
    {
	return super.getStackTrace();
    }

    /**
     * Sets the stack trace elements that will be returned by getStackTrace() and
     * printed by printStackTrace() and related methods. This method, which is
     * designed for use by RPC frameworks and other advanced systems, allows the
     * client to override the default stack trace that is either generated by
     * fillInStackTrace() when a throwable is constructed or deserialized when a
     * throwable is read from a serialization stream. 
     *
     * @param stackTrace the stack trace elements to be associated with this
     *                   Throwable. The specified array is copied by this call;
     *	   	         changes in the specified array after the method invocation
     *                   returns will have no affect on this Throwable's stack trace. 
     * @return NullPointerException - if stackTrace is null, or if any of the
     *         elements of stackTrace are null
     */
    @SpcfXmlBinderExcludeElement
    @Override
    public void setStackTrace(StackTraceElement[] stackTrace)
    {
	super.setStackTrace(stackTrace);
    }

}
