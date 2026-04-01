package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 27, 2008
 * Time: 1:10:16 PM
 */
public class SAPFraudEvent {
    private String fraudIndicator;
    private String companyName;
    private String companyId;
    private String sourceSystemCd;
    private String companyEin;
    private String employeeName;
    private double payrollAmount;
    private Date eventTimeStamp;
    private String details;
    private boolean isFraudFlagSet;

    //  Fraud Details
    private String sourcePayRunId;
    private Date payrollRunDate;
    private Date payrollCheckDate;
    private String payrollRunStatus;

    public SAPFraudEvent() {
        
    }

    public SAPFraudEvent(String fraudIndicator, String companyName, String companyId, String sourceSystemCd, String companyEin, String employeeName, double payrollAmount, Date eventTimeStamp, String details, boolean fraudFlagSet, String sourcePayRunId, Date payrollRunDate, Date payrollCheckDate, String payrollRunStatus) {
        this.fraudIndicator = fraudIndicator;
        this.companyName = companyName;
        this.companyId = companyId;
        this.sourceSystemCd = sourceSystemCd;
        this.companyEin = companyEin;
        this.employeeName = employeeName;
        this.payrollAmount = payrollAmount;
        this.eventTimeStamp = eventTimeStamp;
        this.details = details;
        isFraudFlagSet = fraudFlagSet;
        this.sourcePayRunId = sourcePayRunId;
        this.payrollRunDate = payrollRunDate;
        this.payrollCheckDate = payrollCheckDate;
        this.payrollRunStatus = payrollRunStatus;
    }

    public Date getPayrollRunDate() {
        return payrollRunDate;
    }

    public void setPayrollRunDate(Date payrollRunDate) {
        this.payrollRunDate = payrollRunDate;
    }

    public Date getPayrollCheckDate() {
        return payrollCheckDate;
    }

    public void setPayrollCheckDate(Date payrollCheckDate) {
        this.payrollCheckDate = payrollCheckDate;
    }

    public String getPayrollRunStatus() {
        return payrollRunStatus;
    }

    public void setPayrollRunStatus(String payrollRunStatus) {
        this.payrollRunStatus = payrollRunStatus;
    }

    public String getSourcePayRunId() {
        return sourcePayRunId;
    }

    public void setSourcePayRunId(String sourcePayRunId) {
        this.sourcePayRunId = sourcePayRunId;
    }

    public String getFraudIndicator() {
        return fraudIndicator;
    }

    public void setFraudIndicator(String fraudIndicator) {
        this.fraudIndicator = fraudIndicator;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String sourceSystemCd) {
        this.sourceSystemCd = sourceSystemCd;
    }

    public String getCompanyEin() {
        return companyEin;
    }

    public void setCompanyEin(String companyEin) {
        this.companyEin = companyEin;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public double getPayrollAmount() {
        return payrollAmount;
    }

    public void setPayrollAmount(double payrollAmount) {
        this.payrollAmount = payrollAmount;
    }

    public Date getEventTimeStamp() {
        return eventTimeStamp;
    }

    public void setEventTimeStamp(Date eventTimeStamp) {
        this.eventTimeStamp = eventTimeStamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isFraudFlagSet() {
        return isFraudFlagSet;
    }

    public void setFraudFlagSet(boolean fraudFlagSet) {
        isFraudFlagSet = fraudFlagSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SAPFraudEvent that = (SAPFraudEvent) o;

        if (isFraudFlagSet != that.isFraudFlagSet) return false;
        if (Double.compare(that.payrollAmount, payrollAmount) != 0) return false;
        if (companyEin != null ? !companyEin.equals(that.companyEin) : that.companyEin != null) return false;
        if (companyId != null ? !companyId.equals(that.companyId) : that.companyId != null) return false;
        if (companyName != null ? !companyName.equals(that.companyName) : that.companyName != null) return false;
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (employeeName != null ? !employeeName.equals(that.employeeName) : that.employeeName != null) return false;
        if (!dateNullEquals(eventTimeStamp, that.eventTimeStamp))
            return false;
        if (fraudIndicator != null ? !fraudIndicator.equals(that.fraudIndicator) : that.fraudIndicator != null)
            return false;
        if (!dateNullEquals(payrollCheckDate, that.payrollCheckDate))
            return false;
        if (!dateNullEquals(payrollRunDate, that.payrollRunDate))
            return false;
        if (payrollRunStatus != null ? !payrollRunStatus.equals(that.payrollRunStatus) : that.payrollRunStatus != null)
            return false;
        return !(sourcePayRunId != null ? !sourcePayRunId.equals(that.sourcePayRunId) : that.sourcePayRunId != null) && !(sourceSystemCd != null ? !sourceSystemCd.equals(that.sourceSystemCd) : that.sourceSystemCd != null);

    }

    private boolean dateNullEquals(Date thisDate, Date thatDate) {
        if (thisDate == null) {
            return (thatDate == null);
        } else {
            return (thatDate != null);
        }        
    }

    @Override
    public String toString() {
        return "SAPFraudEvent{" +
                "fraudIndicator='" + fraudIndicator + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyId='" + companyId + '\'' +
                ", sourceSystemCd='" + sourceSystemCd + '\'' +
                ", companyEin='" + companyEin + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", payrollAmount=" + payrollAmount +
                ", eventTimeStamp=" + eventTimeStamp +
                ", details='" + details + '\'' +
                ", isFraudFlagSet=" + isFraudFlagSet +
                ", sourcePayRunId='" + sourcePayRunId + '\'' +
                ", payrollRunDate=" + payrollRunDate +
                ", payrollCheckDate=" + payrollCheckDate +
                ", payrollRunStatus='" + payrollRunStatus + '\'' +
                '}';
    }
}
