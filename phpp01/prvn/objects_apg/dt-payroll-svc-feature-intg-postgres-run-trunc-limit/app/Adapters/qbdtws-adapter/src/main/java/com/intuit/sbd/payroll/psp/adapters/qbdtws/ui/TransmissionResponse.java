package com.intuit.sbd.payroll.psp.adapters.qbdtws.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rnorian
 * Date: Mar 16, 2010
 * Time: 10:41:10 PM
 */
public class TransmissionResponse {
    private String companyName;
    private String receivedDate;
    private String summaryStatus;
    private List<ValidationMessage> transmissionMessages = new ArrayList<ValidationMessage>();
    private List<PaycheckValidationMessage> paycheckMessages = new ArrayList<PaycheckValidationMessage>();
    private List<EmployeeValidationMessage> employeeMessages = new ArrayList<EmployeeValidationMessage>();

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getSummaryStatus() {
        return summaryStatus;
    }

    public void setSummaryStatus(String summaryStatus) {
        this.summaryStatus = summaryStatus;
    }

    public List<PaycheckValidationMessage> getPaycheckMessages() {
        return paycheckMessages;
    }

    public void setPaycheckMessages(List<PaycheckValidationMessage> paycheckMessages) {
        this.paycheckMessages = paycheckMessages;
    }

    public int getPaycheckMessageCount() {
        return paycheckMessages.size();
    }

    public List<EmployeeValidationMessage> getEmployeeMessages() {
        return employeeMessages;
    }

    public void setEmployeeMessages(List<EmployeeValidationMessage> employeeMessages) {
        this.employeeMessages = employeeMessages;
    }

    public int getEmployeeMessageCount() {
        return employeeMessages.size();
    }

    public List<ValidationMessage> getTransmissionMessages() {
        return transmissionMessages;
    }

    public void setTransmissionMessages(List<ValidationMessage> transmissionMessages) {
        this.transmissionMessages = transmissionMessages;
    }

    public int getTransmissionMessageCount() {
        return transmissionMessages.size();
    }
}

