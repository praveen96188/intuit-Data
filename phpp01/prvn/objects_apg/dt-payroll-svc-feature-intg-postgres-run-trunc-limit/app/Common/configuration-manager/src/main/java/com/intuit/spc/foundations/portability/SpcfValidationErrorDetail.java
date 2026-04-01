package com.intuit.spc.foundations.portability;

//import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.net.SpcfUrl;

/**
 * SpcfValidationErrorDetail acts as a value object to provide applications with context and help around
 * an error that occured during business logic processing.  
 * 
 * 
 * Messages that are populated in this class are intended to be localized.
 * 
 * 
 * @author Intuit
 *
 */
public class SpcfValidationErrorDetail
{
    /**
     * The localized problem description that is intended to be user-facing.
     */
    private String mProblemMessage;
    
    /**
     * The localized solution description that is intended to be user-facing.
     */
    private String mSolutionMessage;
    
    /**
     * The message intended for supportability purposes that is targeted for logs
     * or an audit database.
     */
    private String mSupportMessage;
    
    /**
     * TBD - perhaps this should be in an enum (System, Security, Domain, etc).
     */
    private String mCategory;

    /**
     * TBD - perhaps this should be in an enum (Warning, Critical, Info, etc).
     */
    private String mSeverity;
    
    /**
     * The help topic identifier to known help content, if known.
     */
    private String mHelpId;
    
    /**
     * The help context identifier, if known.
     */
    private String mHelpContext;
    
    /**
     * The help location, if known.
     */
    private SpcfUrl mHelpURL;
    
    /**
     * A collection of Field identifiers to help resolve the source of the error.
     */
    private SpcfList<SpcfValidationErrorDetailField> mErrorDetailFieldList = null;
    
    /**
     * Default Constructor.
     */
    public SpcfValidationErrorDetail()
    {
        SpcfFactory factory = SpcfFactory.getInstance();
        
        mErrorDetailFieldList = factory.<SpcfValidationErrorDetailField>createArrayList();
    }

    /**
     * @param problemMessage
     * @param solutionMessage
     * @param supportMessage
     * @param category
     * @param severity
     * @param helpId
     * @param helpContext
     * @param helpURL
     */
    public SpcfValidationErrorDetail(String problemMessage, String solutionMessage, String supportMessage, String category, String severity, String helpId, String helpContext, SpcfUrl helpURL)
    {
        SpcfFactory factory = SpcfFactory.getInstance();
        
        mErrorDetailFieldList = factory.<SpcfValidationErrorDetailField>createArrayList();

        this.mProblemMessage = problemMessage;
        this.mSolutionMessage = solutionMessage;
        this.mSupportMessage = supportMessage;
        this.mCategory = category;
        this.mSeverity = severity;
        this.mHelpId = helpId;
        this.mHelpContext = helpContext;
        this.mHelpURL = helpURL;
    }

    /**
     * @return Returns the category.
     */
    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @param category The category to set.
     */
    public void setCategory(String category)
    {
        this.mCategory = category;
    }

    /**
     * @return Returns the errorDetailFieldList.
     */
    public SpcfList<SpcfValidationErrorDetailField> getValidationErrorDetailFieldList()
    {
        return mErrorDetailFieldList;
    }

    /**
     * Add an ValidationErrorDetailField to the list.
     * @param entry to be adsed to the list
     * @return a copy of the entry just added to the list
     */
    public SpcfValidationErrorDetailField addValidationErrorDetailField(SpcfValidationErrorDetailField entry)
    {
        mErrorDetailFieldList.add(entry);
        
        return entry;
    }

    /**
     * @param errorDetailFieldList The errorDetailFieldList to set.
     */
    public void setValidationErrorDetailFieldList(
            SpcfList<SpcfValidationErrorDetailField> errorDetailFieldList)
    {
        this.mErrorDetailFieldList = errorDetailFieldList;
    }

    /**
     * @return Returns the helpContext.
     */
    public String getHelpContext()
    {
        return mHelpContext;
    }

    /**
     * @param helpContext The helpContext to set.
     */
    public void setHelpContext(String helpContext)
    {
        this.mHelpContext = helpContext;
    }

    /**
     * @return Returns the helpId.
     */
    public String getHelpId()
    {
        return mHelpId;
    }

    /**
     * @param helpId The helpId to set.
     */
    public void setHelpId(String helpId)
    {
        this.mHelpId = helpId;
    }

    /**
     * @return Returns the helpURL.
     */
    public SpcfUrl getHelpURL()
    {
        return mHelpURL;
    }

    /**
     * @param helpURL The helpURL to set.
     */
    public void setHelpURL(SpcfUrl helpURL)
    {
        this.mHelpURL = helpURL;
    }

    /**
     * @return Returns the problemMessage.
     */
    public String getProblemMessage()
    {
        return mProblemMessage;
    }

    /**
     * @param problemMessage The problemMessage to set.
     */
    public void setProblemMessage(String problemMessage)
    {
        this.mProblemMessage = problemMessage;
    }

    /**
     * @return Returns the severity.
     */
    public String getSeverity()
    {
        return mSeverity;
    }

    /**
     * @param severity The severity to set.
     */
    public void setSeverity(String severity)
    {
        this.mSeverity = severity;
    }

    /**
     * @return Returns the solutionMessage.
     */
    public String getSolutionMessage()
    {
        return mSolutionMessage;
    }

    /**
     * @param solutionMessage The solutionMessage to set.
     */
    public void setSolutionMessage(String solutionMessage)
    {
        this.mSolutionMessage = solutionMessage;
    }

    /**
     * @return Returns the supportMessage.
     */
    public String getSupportMessage()
    {
        return mSupportMessage;
    }

    /**
     * @param supportMessage The supportMessage to set.
     */
    public void setSupportMessage(String supportMessage)
    {
        this.mSupportMessage = supportMessage;
    }


}
