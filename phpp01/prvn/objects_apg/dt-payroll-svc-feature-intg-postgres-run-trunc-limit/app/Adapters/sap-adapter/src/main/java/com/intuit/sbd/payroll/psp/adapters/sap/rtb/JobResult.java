package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anandp233 on 2/23/14.
 */
public final class JobResult<T> {

    private boolean isSuccess;
    private List<JobMessage> infoMessages;
    private List<JobMessage> warningMessages;
    private List<JobMessage> errorMessages;
    private T result;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean pIsSuccess) {
        isSuccess = pIsSuccess;
    }

    public List<JobMessage> getWarningMessages() {
        if (warningMessages == null) {
            warningMessages = new ArrayList<JobMessage>();
        }
        return warningMessages;
    }

    public void setWarningMessages(List<JobMessage> pWarningMessages) {
        warningMessages = pWarningMessages;
    }

    public List<JobMessage> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<JobMessage>();
        }
        return errorMessages;
    }

    public void setErrorMessages(List<JobMessage> pErrorMessages) {
        errorMessages = pErrorMessages;
    }

    public List<JobMessage> getInfoMessages() {
        if (infoMessages == null) {
            infoMessages = new ArrayList<JobMessage>();
        }
        return infoMessages;
    }

    public void setInfoMessages(List<JobMessage> pInfoMessages) {
        infoMessages = pInfoMessages;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T pResult) {
        result = pResult;
    }

    public void merge(final JobResult pJobResult) {

        if (pJobResult == null) {
            throw new IllegalArgumentException("JobResult is null, while merging it.");
        }
        //merging the all messages only
        getInfoMessages().addAll(pJobResult.getInfoMessages());
        getWarningMessages().addAll(pJobResult.getWarningMessages());
        getErrorMessages().addAll(pJobResult.getErrorMessages());
    }

    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
        String newLine = System.getProperty("line.separator");
        if (newLine == null) newLine = "\n";

        output.append(" Job Result ");
        output.append(newLine);

        output.append(" Success: ");
        output.append(isSuccess);
        output.append(newLine);
        output.append(newLine);

        if (getInfoMessages().size() > 0){
            output.append(" Info Messages ");
            output.append(newLine);
            for (JobMessage message : getInfoMessages()) {
                output.append(message);
            }
            output.append(newLine);
        }

        if (getWarningMessages().size() > 0){
            output.append(" Warning Messages ");
            output.append(newLine);
            for (JobMessage message : getWarningMessages()) {
                output.append(message);
            }
            output.append(newLine);
        }

        if (getErrorMessages().size() > 0){
            output.append(" Error Messages ");
            output.append(newLine);
            for (JobMessage message : getErrorMessages()) {
                output.append(message);
            }
            output.append(newLine);
        }

        return output.toString();
    }

    public void addInfoMessage(String msg, String code) {
        getInfoMessages().add(new JobMessage(msg, code));
    }

    public void addInfoMessage(String msg) {
        getInfoMessages().add(new JobMessage(msg, null));
    }

    public void addErrorMessage(String msg, String code) {
        getErrorMessages().add(new JobMessage(msg, code));
    }

    public void addErrorMessage(String msg) {
        getErrorMessages().add(new JobMessage(msg, null));
    }

    public void addWarningMessage(String msg, String code) {
        getWarningMessages().add(new JobMessage(msg, code));
    }

    public void addWarningMessage(String msg) {
        getWarningMessages().add(new JobMessage(msg, null));
    }


}
