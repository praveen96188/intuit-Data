package com.intuit.spc.foundations.portability;

/**
 * SpcfValidationErrorDetailField holds three detailed validation error messages that participate
 * in a list of these that is held in the SpcfValidationErrorDetail record
 * 
 * Messages that are populated in this class are intended to be localized.
 * 
 * @author Intuit
 *
 */public class SpcfValidationErrorDetailField
{

    /**
     * The context that this error relates to, if relevant.
     */
    private String mContextName;
    
    /**
     * The GUID (or ID) for the context the error relates to.
     */
    private String mContextId;
    
    /**
     * The context attribute this error relates to, if relevant.
     */
    private String mContextAttribute;
    
    /**
     * 
     */
    public SpcfValidationErrorDetailField()
    {
        //No default constructor
    }

    /**
     * @param name 
     * @param id 
     * @param attribute 
     */
    public SpcfValidationErrorDetailField(String name, String id, String attribute)
    {
        super();
        this.mContextName = name;
        this.mContextId = id;
        this.mContextAttribute = attribute;
    }

    /**
     * @return Returns the entityAttribute.
     */
    public String getContextAttribute()
    {
        return mContextAttribute;
    }

    /**
     * @param contextAttribute The contextAttribute to set.
     */
    public void setContextAttribute(String contextAttribute)
    {
        this.mContextAttribute = contextAttribute;
    }

    /**
     * @return Returns the entityId.
     */
    public String getContextId()
    {
        return mContextId;
    }

    /**
     * @param contextId1 
     */
    public void setContextId(String contextId1)
    {
        this.mContextId = contextId1;
    }

    /**
     * @return Returns the entityName.
     */
    public String getContextName()
    {
        return mContextName;
    }

    /**
     * @param contextName1 The contextName to set.
     */
    public void setContextName(String contextName1)
    {
        this.mContextName = contextName1;
    }
}
