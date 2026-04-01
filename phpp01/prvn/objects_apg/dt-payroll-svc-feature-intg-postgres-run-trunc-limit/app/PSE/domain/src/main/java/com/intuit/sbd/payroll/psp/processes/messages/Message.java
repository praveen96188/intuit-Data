package com.intuit.sbd.payroll.psp.processes.messages;

/**
 * The Message class holds an information accompanying the result of a process.
 */
public final class Message {

     /**
     * Actual message info
     *
     * @serial
     */
    protected MessageInfo mMessageInfo = new MessageInfo();
    /**
     * Gets the underlying message info structure
     */
    public MessageInfo GetMessageInfo() {
        return mMessageInfo;
    }

    /**
     * Gets the Message Level of the message.
     *
     * @return MessageLevel not null.
     * @see com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel
     */
    public MessageInfo.MessageLevel getLevel()
    {
        return mMessageInfo.Level;
    }

    /**
     * Sets the Message Level of the message.
     *
     * @param pLevel MessageLevel not null.
     * @throws IllegalArgumentException
     * @see com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel
     */
    public void setLevel(MessageInfo.MessageLevel pLevel)
    {
        if (pLevel == null)
        {
            throw new IllegalArgumentException("Message Level is null");
        }
        this.mMessageInfo.Level = pLevel;
    }

    /**
     * Gets the Message Code of the message.
     *
     * @return String not null.
     */
    public String getMessageCode()
    {
        return mMessageInfo.MessageCode;
    }

    /**
     * Sets the Message Code of the message.
     *
     * @param pMessageCode String not blank.
     */
    protected void setMessageCode(String pMessageCode)
    {
        if (pMessageCode == null || pMessageCode.trim().length() == 0)
        {
            throw new IllegalArgumentException("Message Code is blank: " + pMessageCode);
        }
        this.mMessageInfo.MessageCode = pMessageCode;
    }

    /**
     * Gets the text of the message.
     *
     * @return String not blank.
     */
    public String getMessage()
    {
        return mMessageInfo.Message;
    }

    /**
     * Sets the text of the message.
     *
     * @param pMessage not blank.
     */
    protected void setMessage(String pMessage)
    {
        if (pMessage == null || pMessage.trim().length() == 0)
        {
            throw new IllegalArgumentException("Message is blank: " + pMessage);
        }
        this.mMessageInfo.Message = pMessage;
    }
    
    /**
     * Gets the Source Id of the data entity that is the root cause of the message.
     *
     * @return String nullable.
     */
    public String getSourceId()
    {
        return mMessageInfo.SourceId;
    }

    /**
     * Sets the Source Id of the data entity that is the root cause of the message.
     *
     * @param pSourceId nullable.
     */
    protected void setSourceId(String pSourceId)
    {
        this.mMessageInfo.SourceId = pSourceId;
    }

    /**
     * Gets the EntityName name of the data element that is the root cause of the message.
     *
     * @return EntityName nullable.
     */
    public EntityName getEntityName()
    {
        return mMessageInfo.EntityName;
    }

    /**
     * Sets the EntityName name of the data element that is the root cause of the message.
     *
     * @param pEntityName nullable.
     */
    protected void setEntityName(EntityName pEntityName)
    {
        this.mMessageInfo.EntityName = pEntityName;
    }    


    private StackTraceElement[] stackTrace;

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    //gets the caller of the MessageList method
    public String getInterestingStackElement() {
        boolean foundMessageList = false;
        for (StackTraceElement stackElement : getStackTrace()) {
            if (stackElement.getClassName().contains("MessageList")) {
                foundMessageList = true;
            } else if (foundMessageList) {
                return stackElement.toString();
            }
        }
        return "";
    }

    /**
     * Returns String representation of the Message object for debugging purposes.
     *
     * @return String.
     */
	@Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        result.append(" Message");
        result.append(newLine);

        result.append("   Level: ");
        result.append(mMessageInfo.Level);
        result.append(newLine);

        result.append("   Message Code: ");
        result.append(mMessageInfo.MessageCode);
        result.append(newLine);

        result.append("   Message: ");
        result.append(mMessageInfo.Message);
        result.append(newLine);

        result.append("   Source Id: ");
        result.append(mMessageInfo.SourceId);
        result.append(newLine);

        result.append("   Entity Name: ");
        result.append(mMessageInfo.EntityName);
        result.append(newLine);

        return result.toString();
    }
}