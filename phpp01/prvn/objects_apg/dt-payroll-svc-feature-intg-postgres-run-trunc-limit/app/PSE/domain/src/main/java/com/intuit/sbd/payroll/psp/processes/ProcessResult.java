package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The ProcessResult class holds an outcome of an execution of a Process.
 * The purpose of the ProcessResult class is to communicate result of an the users of PSP.
 *
 * @author Dawn Haddan
 * @author Wiktor Kozlik
 */
public final class ProcessResult<T> implements Serializable
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = 7422463188597863428L;

    /**
     * List of messages
     *
     * @serial
     * @see com.intuit.sbd.payroll.psp.processes.messages.MessageList
     */
    private MessageList messages;

    /**
     *  Allows manual setting of the sucess of a process result
     */
    private boolean isProcessSuccessful;

    private HashMap<ServiceCode, HashSet<String>> warnings;

    /**
     * Default constructor
     */
    public ProcessResult()
    {
        this(null);
    }

    public ProcessResult(MessageInfo.MessageLevel pDefaultMessageLevel) {
        messages = new MessageList();
        isProcessSuccessful = true;
        messages.setDefaultMessageLevel(pDefaultMessageLevel);
    }

    public void addWarings(ServiceCode pService, String pMsg) {
        if (warnings == null) {
            warnings = new HashMap<ServiceCode, HashSet<String>>();
        }

        HashSet<String> msgs = warnings.get(pService);
        if (msgs == null) {
            msgs = new HashSet<String>();
            warnings.put(pService, msgs);
        }
        msgs.add(pMsg);
    }

    public boolean hasCloudWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public HashMap<ServiceCode, HashSet<String>> getWarnings() {
        return warnings;
    }

    /**
     * Gets the Success boolean value.
     *
     * @return boolean.
     */
    public boolean isSuccess()
    {
        //Look at whether there are errors and the value set by a client of the class
        //and if the success of the process has not been changed explicitly
        return (!(messages.containsErrorMessage()) && isProcessSuccessful);
    }

    public boolean hasWarnings()
    {
        return messages.containsWarningMessage();
    }

    public void addMessages(MessageList pList)
    {
        messages.addAll(pList);
    }

    /**
     * Returns the list of messages.
     *
     * @return List<Message> not null.
     */
    public MessageList getMessages()
    {
        return messages;
    }

    /**
     * Returns the list of messages matching the passed in message level.
     *
     * @param pMessageLevel
     * @return List<Message> not null
     */
    public MessageList getMessages(MessageInfo.MessageLevel pMessageLevel) {
        MessageList filteredMessages = new MessageList();
        for (Message message : messages) {
            if (message.getLevel() == pMessageLevel)
                filteredMessages.add(message);
        }

        return filteredMessages;
    }

    public MessageList getMessages(String pMessageCode) {
        MessageList filteredMessages = new MessageList();
        for (Message message : messages) {
            if (message.getMessageCode().equals(pMessageCode))
                filteredMessages.add(message);
        }

        return filteredMessages;
    }

    public MessageList getInfoMessages() {
        return getMessages(MessageInfo.MessageLevel.INFO);
    }

    public MessageList getWarningMessages() {
        return getMessages(MessageInfo.MessageLevel.WARNING);
    }

    public MessageList getErrorMessages() {
        return getMessages(MessageInfo.MessageLevel.ERROR);
    }

    /**
     * Merges other ProcessResult with this ProcessResult.
     * Other's messages are appended to the list of messages of this object.
     * Other's success value is combined with the success value of this object.
     *
     * @param pProcessResult final ProcessResult not null.
     */
    public void merge(final ProcessResult pProcessResult)
    {
        if (pProcessResult == null)
        {
            throw new IllegalArgumentException("Process Result is null");
        }
        this.getMessages().addAll(pProcessResult.getMessages());

        // merge warnings
        if (pProcessResult.hasCloudWarnings()) {
            if (!hasCloudWarnings()) {
                warnings = pProcessResult.getWarnings();
            } else {
                for (ServiceCode key : (Set<ServiceCode>)pProcessResult.getWarnings().keySet()) {
                    if (warnings.containsKey(key)) {
                        warnings.get(key).addAll((Set<String>)pProcessResult.getWarnings().get(key));
                    } else {
                        warnings.put(key, (HashSet<String>)pProcessResult.getWarnings().get(key));
                    }
                }
            }
        }
    }

    /**
     * Returns String representation of the ProcessResult object for debugging purposes.
     *
     * @return String.
     */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        result.append("Process Result");
        result.append(newLine);

        result.append(" Success: ");
        result.append(isSuccess());
        result.append(newLine);

        result.append(" Messages: ");
        result.append(newLine);

        for (Message currMessage : messages)
        {
            result.append(currMessage);
        }

        return result.toString();
    }

    /**
     * Sets the success of the process result
     * @param pSuccess
     */
    public void setSuccess(boolean pSuccess) {
        isProcessSuccessful = pSuccess;
    }

    private T result;
    public T getResult() {
        return result;
    }
    public void setResult(T pResult) {
        result = pResult;
    }
}

