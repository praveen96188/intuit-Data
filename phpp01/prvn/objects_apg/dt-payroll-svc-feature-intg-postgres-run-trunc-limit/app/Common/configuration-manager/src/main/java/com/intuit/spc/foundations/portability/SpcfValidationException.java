package com.intuit.spc.foundations.portability;

import com.intuit.spc.foundations.portability.collections.SpcfList;

/**
 * Portable exception class.
 *  System.SpcfNullReferenceException
 *  java.lang.NullPointerException 
 */
public class SpcfValidationException extends SpcfRuntimeException 
{
   
    /**
     * 
     */
    private static final long serialVersionUID = 7870119241014442487L;
    
    /**
     * Variable to hold the Error Details
     */
    private SpcfList<SpcfValidationErrorDetail>  mErrorDetailList = null;
    
 	/**
	 * Class constructor
	 */
	public SpcfValidationException() 
    {
        //Empty constructor
    }
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfValidationException(String message, Throwable e) { super(message, e); }
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfValidationException(Throwable e) { super(e); }
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfValidationException(int applicationId, int architectureId, int moduleId) 
	{ 
		super(applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfValidationException(String message, int applicationId, int architectureId, int moduleId) 
	{ 
		super(message, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfValidationException(String message, Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(message, e, applicationId, architectureId, moduleId); 
	}
	
	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfValidationException(Throwable e, int applicationId, int architectureId, int moduleId) 
	{ 
		super(e, applicationId, architectureId, moduleId); 
	}
	
    /**
     * @return Returns the errorDetailList.
     */
    public SpcfList<SpcfValidationErrorDetail> getErrorDetailList()
    {
        return mErrorDetailList;
    }

    /**
     * @param errorDetailList The errorDetailList to set.
     */
    public void setErrorDetailList(SpcfList<SpcfValidationErrorDetail> errorDetailList)
    {
        this.mErrorDetailList = errorDetailList;
    }

}
