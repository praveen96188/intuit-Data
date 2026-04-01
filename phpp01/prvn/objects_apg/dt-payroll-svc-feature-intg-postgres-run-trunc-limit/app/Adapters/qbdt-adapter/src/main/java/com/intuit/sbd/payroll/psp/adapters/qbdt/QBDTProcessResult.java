package com.intuit.sbd.payroll.psp.adapters.qbdt;

import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;

/**
 * The ProcessResult class holds an outcome of an execution of a Process.
 * The purpose of the ProcessResult class is to communicate result of an the users of PSP.
 *
 * @author Dawn Haddan
 * @author Wiktor Kozlik
 */
public final class QBDTProcessResult<T> {

    /**
     *  Allows manual setting of the sucess of a process result
     */
    private boolean isProcessSuccessful;

    private ErrorMessage message;

    // List of company events that need to be created.
    private List<QBDTCompanyEventDTO> companyEventList = new LinkedList<QBDTCompanyEventDTO>();

    /**
     * Default constructor
     */
    public QBDTProcessResult()
    {
        isProcessSuccessful = true;
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
        return (message==null && isProcessSuccessful);
    }

    /**
     * Set error message on this object.
     *
     * @param errorMessage - Error message.
     */
    public void setMessage(ErrorMessage errorMessage)
    {
        message=errorMessage;
    }

    /**
     * Returns the list of messages.
     *
     * @return List<Message> not null.
     */
    public ErrorMessage getMessage()
    {
        return message;
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

        result.append(super.toString().split("@")[0]);
        result.append(newLine);

        result.append(" Success: ");
        result.append(isSuccess());
        result.append(newLine);

        result.append(" Message: ");
        result.append(newLine);

        result.append(message);

        return result.toString();
    }

    /**
     *
     * @param processResultToCopy
     */
    public void copySuccess(QBDTProcessResult processResultToCopy) {
        this.addCompanyEvents(processResultToCopy.getCompanyEventList());
    }

    /**
     * Sets the success of the process result
     *
     * @param pSuccess - Process success flag.
     */
    public void setSuccess(boolean pSuccess) {
        isProcessSuccessful = pSuccess;
    }

    /**
     *
     * @param coEventDTO
     */
    public void addCompanyEvent(QBDTCompanyEventDTO coEventDTO) {
        companyEventList.add(coEventDTO);
    }

    /**
     *
     * @param coEventListToAdd
     */
    public void addCompanyEvents(List<QBDTCompanyEventDTO> coEventListToAdd) {
        companyEventList.addAll(coEventListToAdd);
    }

    /**
     *
     * @return
     */
    public List<QBDTCompanyEventDTO> getCompanyEventList() {
        return companyEventList;
    }

    private T result;

    /**
     * Return result object.
     * @return Result object.
     */
    public T getResult() {
        return result;
    }

    /**
     * Set result object.
     *
     * @param pResult - Result object.
     */
    public void setResult(T pResult) {
        result = pResult;
    }

    private CredentialType mCredentialType;

    public CredentialType getCredentialType() {
        return mCredentialType;
    }

    public void setCredentialType(CredentialType pCredentialType) {
        mCredentialType = pCredentialType;
    }
}

